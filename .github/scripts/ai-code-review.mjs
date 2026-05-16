const MARKER = '<!-- blinkly-ai-code-review -->';
const DEFAULT_MODEL = 'gpt-5.4-nano';
const DEFAULT_MAX_REVIEW_CHARS = 24000;
const DEFAULT_MAX_AGENT_GUIDE_CHARS = 12000;
const DEFAULT_MAX_OUTPUT_TOKENS = 2000;
const DEFAULT_EXCLUDED_EXTENSIONS = [
  '.png',
  '.jpg',
  '.jpeg',
  '.gif',
  '.webp',
  '.avif',
  '.ico',
  '.pdf',
  '.zip',
  '.gz',
  '.tgz',
  '.mp3',
  '.mp4',
  '.mov',
  '.webm',
  '.ttf',
  '.otf',
  '.woff',
  '.woff2',
];
const DEFAULT_EXCLUDED_PATHS = [
  'gradle/wrapper/gradle-wrapper.jar',
];

const env = process.env;
const githubToken = env.GITHUB_TOKEN;
const openAiKey = env.OPENAI_API_KEY;
const repository = env.GITHUB_REPOSITORY;
const prNumber = env.PR_NUMBER;
const prHeadSha = env.PR_HEAD_SHA || env.GITHUB_SHA || 'unknown';
const stepSummaryPath = env.GITHUB_STEP_SUMMARY;
const model = env.OPENAI_REVIEW_MODEL || DEFAULT_MODEL;
const maxReviewChars = Number(env.MAX_REVIEW_CHARS || DEFAULT_MAX_REVIEW_CHARS);
const maxAgentGuideChars = Number(env.MAX_AGENT_GUIDE_CHARS || DEFAULT_MAX_AGENT_GUIDE_CHARS);
const maxOutputTokens = Number(env.OPENAI_MAX_OUTPUT_TOKENS || DEFAULT_MAX_OUTPUT_TOKENS);
const excludedExtensions = parseListEnv(env.AI_REVIEW_EXCLUDED_EXTENSIONS, DEFAULT_EXCLUDED_EXTENSIONS)
  .map((extension) => extension.toLowerCase());
const excludedPaths = parseListEnv(env.AI_REVIEW_EXCLUDED_PATHS, DEFAULT_EXCLUDED_PATHS);

main().catch(async (error) => {
  console.error(error);
  await tryPostDiagnostic(`AI code review could not complete: ${formatError(error)}`);
  process.exit(0);
});

async function main() {
  assertRequiredEnv();

  if (!openAiKey) {
    await postDiagnostic('AI code review is not configured: missing `OPENAI_API_KEY` repository secret.');
    return;
  }

  const agentGuide = truncateText(
    await readTextFile('AGENTS.md'),
    sanitizePositiveNumber(maxAgentGuideChars, DEFAULT_MAX_AGENT_GUIDE_CHARS),
  );
  const files = await fetchPullRequestFiles();
  const { diffText, truncated, includedFiles, skippedFiles } = buildReviewDiff(files);

  if (!diffText.trim()) {
    await publishComment(renderComment({
      summary: 'No reviewable text diff was found.',
      findings: [],
      truncated,
      includedFiles,
      skippedFiles,
    }));
    return;
  }

  const review = await requestOpenAiReview(agentGuide, diffText, truncated);
  await publishComment(renderComment({
    summary: review.summary,
    findings: review.findings,
    truncated,
    includedFiles,
    skippedFiles,
  }));
}

function assertRequiredEnv() {
  const missing = [];
  if (!githubToken) missing.push('GITHUB_TOKEN');
  if (!repository) missing.push('GITHUB_REPOSITORY');
  if (!prNumber) missing.push('PR_NUMBER');
  if (missing.length > 0) {
    throw new Error(`Missing required environment variables: ${missing.join(', ')}`);
  }
}

async function readTextFile(path) {
  const fs = await import('node:fs/promises');
  return fs.readFile(path, 'utf8');
}

async function fetchPullRequestFiles() {
  const allFiles = [];
  for (let page = 1; ; page += 1) {
    const url = githubApiUrl(`/repos/${repository}/pulls/${prNumber}/files`, {
      per_page: '100',
      page: String(page),
    });
    const batch = await githubRequest(url);
    allFiles.push(...batch);
    if (batch.length < 100) return allFiles;
  }
}

function buildReviewDiff(files) {
  const included = [];
  const skipped = [];
  let remaining = sanitizePositiveNumber(maxReviewChars, DEFAULT_MAX_REVIEW_CHARS);
  let truncated = false;
  let text = '';

  for (const file of files) {
    const skipReason = getReviewSkipReason(file.filename);
    if (skipReason) {
      skipped.push(`${file.filename} (${file.status}, ${skipReason})`);
      continue;
    }

    if (!file.patch) {
      skipped.push(`${file.filename} (${file.status}, no text patch)`);
      continue;
    }

    const section = [
      `File: ${file.filename}`,
      `Status: ${file.status}; additions: ${file.additions}; deletions: ${file.deletions}`,
      'Patch:',
      file.patch,
      '',
    ].join('\n');

    if (section.length > remaining) {
      text += section.slice(0, Math.max(0, remaining));
      truncated = true;
      included.push(file.filename);
      break;
    }

    text += section;
    remaining -= section.length;
    included.push(file.filename);
  }

  if (included.length + skipped.length < files.length) {
    truncated = true;
    for (const file of files.slice(included.length + skipped.length)) {
      skipped.push(`${file.filename} (${file.status}, prompt limit)`);
    }
  }

  return {
    diffText: text,
    truncated,
    includedFiles: included,
    skippedFiles: skipped,
  };
}

function getReviewSkipReason(filename) {
  const normalized = filename.replaceAll('\\', '/');
  const lower = normalized.toLowerCase();

  if (excludedPaths.some((path) => normalized === path || normalized.startsWith(`${path}/`))) {
    return 'excluded path';
  }

  if (excludedExtensions.some((extension) => lower.endsWith(extension))) {
    return 'excluded file type';
  }

  return '';
}

function parseListEnv(value, fallback) {
  if (typeof value !== 'string' || !value.trim()) {
    return fallback;
  }

  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function sanitizePositiveNumber(value, fallback) {
  return Number.isFinite(value) && value > 0 ? value : fallback;
}

function truncateText(text, maxChars) {
  if (typeof text !== 'string' || text.length <= maxChars) {
    return text;
  }

  return `${text.slice(0, maxChars)}\n\n[Truncated to ${maxChars} characters for the AI review prompt budget.]`;
}

async function requestOpenAiReview(agentGuide, diffText, truncated) {
  const payload = {
    model,
    instructions: [
      'You are a senior Kotlin Multiplatform code reviewer for the Blinkly repository.',
      'Return only high-confidence, actionable findings. Do not invent issues.',
      'Focus on correctness, regressions, Kotlin/KMP compatibility, Decompose/MVIKotlin architecture rules, threading, lifecycle, dependency injection, and missing tests.',
      'Ignore formatting or style unless it violates existing project patterns or can cause maintenance/correctness issues.',
      'The review output must be in English.',
    ].join('\n'),
    input: [
      {
        role: 'user',
        content: [
          {
            type: 'input_text',
            text: [
              'Project guide from AGENTS.md:',
              agentGuide,
              '',
              `The PR diff ${truncated ? 'was truncated because of prompt size limits' : 'is complete within the configured prompt size limit'}.`,
              'Review this pull request diff:',
              diffText,
            ].join('\n'),
          },
        ],
      },
    ],
    max_output_tokens: sanitizePositiveNumber(maxOutputTokens, DEFAULT_MAX_OUTPUT_TOKENS),
    text: {
      format: {
        type: 'json_schema',
        name: 'blinkly_code_review',
        strict: true,
        schema: reviewSchema(),
      },
    },
  };

  const response = await fetch('https://api.openai.com/v1/responses', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${openAiKey}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  const bodyText = await response.text();
  if (!response.ok) {
    throw new Error(`OpenAI API request failed with ${response.status}: ${bodyText}`);
  }

  const body = JSON.parse(bodyText);
  const outputText = extractOpenAiOutputText(body);
  const parsed = JSON.parse(outputText);

  return {
    summary: stringOrDefault(parsed.summary, 'AI review completed.'),
    findings: Array.isArray(parsed.findings) ? parsed.findings : [],
  };
}

function reviewSchema() {
  return {
    type: 'object',
    additionalProperties: false,
    required: ['summary', 'findings'],
    properties: {
      summary: {
        type: 'string',
        description: 'A concise one or two sentence review summary.',
      },
      findings: {
        type: 'array',
        description: 'High-confidence code review findings ordered by severity.',
        items: {
          type: 'object',
          additionalProperties: false,
          required: ['severity', 'file', 'line', 'title', 'details', 'suggestion'],
          properties: {
            severity: {
              type: 'string',
              enum: ['critical', 'high', 'medium', 'low'],
            },
            file: {
              type: 'string',
              description: 'Repository-relative path of the affected file.',
            },
            line: {
              type: 'string',
              description: 'Changed line number as a string, or empty string when unavailable.',
            },
            title: {
              type: 'string',
              description: 'Short finding title.',
            },
            details: {
              type: 'string',
              description: 'Why this is a real issue in this PR.',
            },
            suggestion: {
              type: 'string',
              description: 'Concrete fix or next action.',
            },
          },
        },
      },
    },
  };
}

function extractOpenAiOutputText(body) {
  if (typeof body.output_text === 'string') {
    return body.output_text;
  }

  const texts = [];
  for (const item of body.output || []) {
    for (const content of item.content || []) {
      if (content.type === 'output_text' && typeof content.text === 'string') {
        texts.push(content.text);
      }
    }
  }

  if (texts.length === 0) {
    throw new Error(`OpenAI response did not contain output text: ${JSON.stringify(body)}`);
  }
  return texts.join('\n');
}

function renderComment({ summary, findings, truncated, includedFiles, skippedFiles }) {
  const lines = [
    MARKER,
    '## AI Code Review',
    '',
    `Model: \`${model}\``,
    `Commit: \`${prHeadSha}\``,
    `Diff truncated: \`${truncated ? 'yes' : 'no'}\``,
    '',
    `Reviewable files: ${includedFiles.length}`,
    skippedFiles.length > 0 ? `Skipped files: ${skippedFiles.length}` : '',
    '',
    summary,
    '',
  ].filter(Boolean);

  if (!findings || findings.length === 0) {
    lines.push('No high-confidence issues found.');
  } else {
    lines.push('### Findings', '');
    for (const finding of findings) {
      const location = finding.line ? `${finding.file}:${finding.line}` : finding.file;
      lines.push(
        `- **${normalizeSeverity(finding.severity)}** \`${location}\` - ${stringOrDefault(finding.title, 'Untitled finding')}`,
        `  ${stringOrDefault(finding.details, 'No details provided.')}`,
        `  Suggested fix: ${stringOrDefault(finding.suggestion, 'No suggestion provided.')}`,
        '',
      );
    }
  }

  if (skippedFiles.length > 0) {
    lines.push(
      '',
      '<details>',
      '<summary>Skipped files</summary>',
      '',
      ...skippedFiles.map((file) => `- \`${file}\``),
      '',
      '</details>',
    );
  }

  lines.push('', '_AI review supplements human review and CI; validate findings before acting._');
  return lines.join('\n');
}

async function postDiagnostic(message) {
  if (!githubToken || !repository || !prNumber) {
    console.warn(message);
    await appendStepSummary(message);
    return;
  }

  await publishComment([
    MARKER,
    '## AI Code Review',
    '',
    message,
    '',
    '_The workflow is non-blocking, so this diagnostic does not fail the PR._',
  ].join('\n'));
}

async function tryPostDiagnostic(message) {
  try {
    await postDiagnostic(message);
  } catch (error) {
    console.warn(`Could not post AI code review diagnostic: ${formatError(error)}`);
    await appendStepSummary(message);
  }
}

async function publishComment(body) {
  try {
    await upsertComment(body);
  } catch (error) {
    if (!isPermissionDeniedError(error)) {
      throw error;
    }

    console.warn(`Could not post AI code review comment: ${formatError(error)}`);
    await appendStepSummary(body);
  }
}

async function upsertComment(body) {
  const comments = await listIssueComments();
  const existing = comments.find((comment) => typeof comment.body === 'string' && comment.body.includes(MARKER));

  if (existing) {
    await githubRequest(githubApiUrl(`/repos/${repository}/issues/comments/${existing.id}`), {
      method: 'PATCH',
      body: JSON.stringify({ body }),
    });
    return;
  }

  await githubRequest(githubApiUrl(`/repos/${repository}/issues/${prNumber}/comments`), {
    method: 'POST',
    body: JSON.stringify({ body }),
  });
}

async function listIssueComments() {
  const comments = [];
  for (let page = 1; ; page += 1) {
    const url = githubApiUrl(`/repos/${repository}/issues/${prNumber}/comments`, {
      per_page: '100',
      page: String(page),
    });
    const batch = await githubRequest(url);
    comments.push(...batch);
    if (batch.length < 100) return comments;
  }
}

async function githubRequest(url, options = {}) {
  const response = await fetch(url, {
    method: options.method || 'GET',
    headers: {
      Authorization: `Bearer ${githubToken}`,
      Accept: 'application/vnd.github+json',
      'Content-Type': 'application/json',
      'X-GitHub-Api-Version': '2022-11-28',
      ...(options.headers || {}),
    },
    body: options.body,
  });

  const text = await response.text();
  if (!response.ok) {
    const error = new Error(`GitHub API request failed with ${response.status}: ${text}`);
    error.githubStatus = response.status;
    throw error;
  }
  return text ? JSON.parse(text) : null;
}

function githubApiUrl(path, query = {}) {
  const url = new URL(`https://api.github.com${path}`);
  for (const [key, value] of Object.entries(query)) {
    url.searchParams.set(key, value);
  }
  return url;
}

async function appendStepSummary(markdown) {
  if (!stepSummaryPath) {
    console.warn(markdown);
    return;
  }

  const fs = await import('node:fs/promises');
  await fs.appendFile(stepSummaryPath, `${markdown}\n`, 'utf8');
}

function isPermissionDeniedError(error) {
  return error && error.githubStatus === 403;
}

function normalizeSeverity(severity) {
  const value = stringOrDefault(severity, 'medium').toLowerCase();
  return value.charAt(0).toUpperCase() + value.slice(1);
}

function stringOrDefault(value, fallback) {
  return typeof value === 'string' && value.trim() ? value.trim() : fallback;
}

function formatError(error) {
  return error && error.message ? error.message : String(error);
}
