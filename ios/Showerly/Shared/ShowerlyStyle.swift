import SwiftUI

enum ShowerlyStyle {
    private static let emptyRGB = (46.0 / 255, 139.0 / 255, 1.0)
    private static let busyRGB = (1.0, 193.0 / 255, 7.0 / 255)
    private static let fullRGB = (229.0 / 255, 57.0 / 255, 53.0 / 255)

    static func crowdColor(_ ratio: Double) -> Color {
        let value = min(max(ratio, 0), 1)
        if value <= 0.6 {
            return mix(emptyRGB, busyRGB, amount: value / 0.6)
        }
        return mix(busyRGB, fullRGB, amount: (value - 0.6) / 0.4)
    }

    private static func mix(
        _ start: (Double, Double, Double),
        _ end: (Double, Double, Double),
        amount: Double
    ) -> Color {
        return Color(
            red: start.0 + (end.0 - start.0) * amount,
            green: start.1 + (end.1 - start.1) * amount,
            blue: start.2 + (end.2 - start.2) * amount
        )
    }
}

extension View {
    func showerlyGlassCard(cornerRadius: CGFloat = 28) -> some View {
        glassEffect(.regular, in: .rect(cornerRadius: cornerRadius))
    }
}
