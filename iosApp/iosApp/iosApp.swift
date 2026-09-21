import SwiftUI
import compose
import FirebaseAnalytics
import FirebaseCore

@main
struct ComposeApp: App {
    private let analyticsReporter: BlinklyIosAnalyticsReporter

    init() {
        FirebaseApp.configure()
        let reporter = FirebaseBlinklyAnalyticsReporter()
#if DEBUG
        Analytics.setAnalyticsCollectionEnabled(false)
#else
        let bootstrapState = MainKt.GetBlinklyAnalyticsBootstrapState()
        Analytics.setAnalyticsCollectionEnabled(bootstrapState.analyticsEnabled)
        reporter.activate()
#endif
        analyticsReporter = reporter
    }

    var body: some Scene {
        WindowGroup {
            ContentView(analyticsReporter: analyticsReporter).ignoresSafeArea(.all)
        }
    }
}

struct ContentView: UIViewControllerRepresentable {
    let analyticsReporter: BlinklyIosAnalyticsReporter

    func makeUIViewController(context: Context) -> UIViewController {
        return MainKt.MainViewController(analyticsReporter: analyticsReporter)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates will be handled by Compose
    }
}

private final class FirebaseBlinklyAnalyticsReporter: NSObject, BlinklyIosAnalyticsReporter {
    private var active = false

    func activate() {
        active = true
    }

    func reportEvent(name: String, parameters: [String: String]) {
        guard active else { return }
        let firebaseParameters = parameters.reduce(into: [String: Any]()) { result, entry in
            result[entry.key] = entry.value
        }
        Analytics.logEvent(name, parameters: firebaseParameters)
    }

    func setDataSendingEnabled(enabled: Bool) {
        guard active else { return }
        Analytics.setAnalyticsCollectionEnabled(enabled)
    }
}
