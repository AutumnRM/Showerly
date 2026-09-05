import Foundation
import SwiftUI

enum Gender: String, CaseIterable, Codable, Identifiable, Sendable {
    case male
    case female

    var id: Self { self }
    var label: String { self == .male ? "男" : "女" }
    var sex: Int { self == .male ? 0 : 1 }
}
enum Campus: String, CaseIterable, Codable, Identifiable, Sendable {
    case changan
    case taibai

    var id: Self { self }

    var label: String {
        switch self {
        case .changan: "长安校区"
        case .taibai: "太白校区"
        }
    }

    var campusID: String {
        switch self {
        case .changan: "4"
        case .taibai: "36"
        }
    }
}

enum AppearancePreference: String, CaseIterable, Codable, Identifiable, Sendable {
    case system
    case light
    case dark

    var id: Self { self }

    var label: String {
        switch self {
        case .system: "跟随系统"
        case .light: "浅色"
        case .dark: "深色"
        }
    }

    var colorScheme: ColorScheme? {
        switch self {
        case .system: nil
        case .light: .light
        case .dark: .dark
        }
    }
}

struct AppSettings: Equatable, Sendable {
    var gender: Gender = .male
    var campus: Campus = .changan
    var appearance: AppearancePreference = .system
}

struct CrowdAPIResponse: Decodable, Sendable {
    let code: String?
    let message: String?
    let data: [BathroomDTO]?

    enum CodingKeys: String, CodingKey {
        case code
        case message = "msg"
        case data
    }
}

struct BathroomDTO: Decodable, Equatable, Sendable {
    let id: Int?
    let name: String?
    let sex: Int?
    let maxLoad: Int?
    let useCount: Int?
    let bookingDeviceCount: Int?
    let availableBookingDeviceCount: Int?

    enum CodingKeys: String, CodingKey {
        case id, name, sex, maxLoad, useCount
        case bookingDeviceCount = "bookingDeviceCnt"
        case availableBookingDeviceCount = "availableBookingDeviceCnt"
    }

    func toStatus() -> BathroomStatus {
        let maximum = max(maxLoad ?? 0, 0)
        let use = max(useCount ?? 0, 0)
        let capacity = max(maximum, use)
        let vacant = max(capacity - use, 0)
        let rawRatio = capacity > 0 ? Double(use) / Double(capacity) : 0
        let ratio = min(max(rawRatio, 0), 1)

        let label: String
        switch (capacity, ratio) {
        case (0, _): label = "未知"
        case (_, 0.9...): label = "爆满"
        case (_, 0.6...): label = "较拥挤"
        case (_, let value) where value > 0: label = "正常"
        default: label = "空闲"
        }

        return BathroomStatus(
            id: id ?? 0,
            name: name ?? "未知浴室",
            sex: sex ?? 2,
            maxLoad: maximum,
            useCount: use,
            vacant: vacant,
            capacity: capacity,
            occupancyRatio: ratio,
            statusLabel: label
        )
    }
}

struct BathroomStatus: Identifiable, Equatable, Sendable {
    let id: Int
    let name: String
    let sex: Int
    let maxLoad: Int
    let useCount: Int
    let vacant: Int
    let capacity: Int
    let occupancyRatio: Double
    let statusLabel: String

    var advice: (title: String, detail: String) {
        switch (capacity, occupancyRatio) {
        case (0, _): ("暂无容量信息", "可以稍后刷新再试")
        case (_, 0.9...): ("接近满员", "建议错峰前往，避免长时间等待")
        case (_, 0.6...): ("当前人流较多", "可能需要短暂等待")
        case (_, 0.3...): ("空位较充足", "现在前往通常无需久等")
        default: ("当前很空闲", "现在是不错的洗浴时段")
        }
    }
}
