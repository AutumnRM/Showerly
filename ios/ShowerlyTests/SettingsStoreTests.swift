import XCTest
@testable import Showerly

@MainActor
final class SettingsStoreTests: XCTestCase {
    func testDefaultsAndPersistence() {
        let suiteName = "SettingsStoreTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let initial = SettingsStore(defaults: defaults)
        XCTAssertEqual(initial.snapshot, AppSettings())

        initial.gender = .female
        initial.campus = .taibai
        initial.appearance = .dark

        let restored = SettingsStore(defaults: defaults)
        XCTAssertEqual(
            restored.snapshot,
            AppSettings(gender: .female, campus: .taibai, appearance: .dark)
        )
    }
}
