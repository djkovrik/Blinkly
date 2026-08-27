import SwiftUI
import compose
import FirebaseCore
import AppMetricaCore

@main
struct ComposeApp: App {
    private let analyticsReporter: BlinklyAnalyticsReporter

    init() {
        FirebaseApp.configure()
        let reporter = AppMetricaBlinklyAnalyticsReporter()
#if !DEBUG
        let bootstrapState = MainKt.GetBlinklyAnalyticsBootstrapState()
        reporter.activate(
            apiKey: Bundle.main.object(forInfoDictionaryKey: "BlinklyAppMetricaApiKey") as? String ?? "",
            dataSendingEnabled: bootstrapState.analyticsEnabled,
            existingInstallation: bootstrapState.existingInstallation
        )
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
    let analyticsReporter: BlinklyAnalyticsReporter

    func makeUIViewController(context: Context) -> UIViewController {
        return MainKt.MainViewController(analyticsReporter: analyticsReporter)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Updates will be handled by Compose
    }
}

private final class AppMetricaBlinklyAnalyticsReporter: NSObject, BlinklyAnalyticsReporter {
    private var active = false

    func activate(apiKey: String, dataSendingEnabled: Bool, existingInstallation: Bool) {
        guard let configuration = AppMetricaConfiguration(apiKey: apiKey) else {
            assertionFailure("Unable to create AppMetrica configuration")
            return
        }

        configuration.locationTracking = false
        configuration.revenueAutoTrackingEnabled = false
        configuration.dataSendingEnabled = dataSendingEnabled
        configuration.handleFirstActivationAsUpdate = existingInstallation
        AppMetrica.activate(with: configuration)
        active = true
    }

    func reportEvent(name: String, parameters: [String: String]) {
        guard active else { return }
        AppMetrica.reportEvent(name, parameters: parameters, onFailure: nil)
    }

    func setDataSendingEnabled(enabled: Bool) {
        guard active else { return }
        AppMetrica.setDataSendingEnabled(enabled)
    }
}
