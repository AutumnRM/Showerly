import Foundation
import Observation

@Observable
@MainActor
final class SettingsStore {
    private enum Key {
        static let gender = "showerly.gender"
        static let campus = "showerly.campus"
        static let appearance = "showerly.appearance"
    }

    private let defaults: UserDefaults

    var gender: Gender {
        didSet { defaults.set(gender.rawValue, forKey: Key.gender) }
    }

    var campus: Campus {
        didSet { defaults.set(campus.rawValue, forKey: Key.campus) }
    }

    var appearance: AppearancePreference {
        didSet { defaults.set(appearance.rawValue, forKey: Key.appearance) }
    }

    init(defaults: UserDefaults = .standard, reset: Bool = false) {
        self.defaults = defaults
        if reset {
            defaults.removeObject(forKey: Key.gender)
            defaults.removeObject(forKey: Key.campus)
            defaults.removeObject(forKey: Key.appearance)
        }
        gender = Gender(rawValue: defaults.string(forKey: Key.gender) ?? "") ?? .male
        campus = Campus(rawValue: defaults.string(forKey: Key.campus) ?? "") ?? .changan
        appearance = AppearancePreference(rawValue: defaults.string(forKey: Key.appearance) ?? "") ?? .system
    }

    var snapshot: AppSettings {
        AppSettings(gender: gender, campus: campus, appearance: appearance)
    }

    var requestSelection: RequestSelection {
        RequestSelection(gender: gender, campus: campus)
    }
}

struct RequestSelection: Hashable, Sendable {
    let gender: Gender
    let campus: Campus
}
