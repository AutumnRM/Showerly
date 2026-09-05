#!/usr/bin/env swift

import AppKit
import Foundation

enum IconAppearance {
    case light
    case dark
    case tinted
}

let outputDirectory = URL(fileURLWithPath: CommandLine.arguments.dropFirst().first ?? ".", isDirectory: true)
let canvasSize = NSSize(width: 1024, height: 1024)

func color(_ hex: UInt32, alpha: CGFloat = 1) -> NSColor {
    NSColor(
        red: CGFloat((hex >> 16) & 0xff) / 255,
        green: CGFloat((hex >> 8) & 0xff) / 255,
        blue: CGFloat(hex & 0xff) / 255,
        alpha: alpha
    )
}

func drawIcon(appearance: IconAppearance) {
    let bounds = NSRect(origin: .zero, size: canvasSize)
    let backgroundColors: [NSColor]
    switch appearance {
    case .light: backgroundColors = [color(0x17A2B8), color(0x087F94)]
    case .dark: backgroundColors = [color(0x123849), color(0x04131C)]
    case .tinted: backgroundColors = [color(0xECECEC), color(0xBEBEBE)]
    }
    NSGradient(colors: backgroundColors)!.draw(in: bounds, angle: -90)

    let shadow = NSShadow()
    shadow.shadowColor = .black.withAlphaComponent(appearance == .dark ? 0.45 : 0.18)
    shadow.shadowBlurRadius = 34
    shadow.shadowOffset = NSSize(width: 0, height: -16)
    NSGraphicsContext.current?.saveGraphicsState()
    shadow.set()

    let drop = NSBezierPath()
    drop.move(to: NSPoint(x: 512, y: 840))
    drop.curve(to: NSPoint(x: 292, y: 448), controlPoint1: NSPoint(x: 512, y: 840), controlPoint2: NSPoint(x: 292, y: 590))
    drop.curve(to: NSPoint(x: 512, y: 224), controlPoint1: NSPoint(x: 292, y: 324), controlPoint2: NSPoint(x: 390, y: 224))
    drop.curve(to: NSPoint(x: 732, y: 448), controlPoint1: NSPoint(x: 634, y: 224), controlPoint2: NSPoint(x: 732, y: 324))
    drop.curve(to: NSPoint(x: 512, y: 840), controlPoint1: NSPoint(x: 732, y: 590), controlPoint2: NSPoint(x: 512, y: 840))
    drop.close()

    let gradientColors: [NSColor]
    switch appearance {
    case .light, .dark:
        gradientColors = [color(0xE1F5FE), color(0x4FC3F7), color(0x0277BD)]
    case .tinted:
        gradientColors = [color(0xFFFFFF), color(0x8A8A8A), color(0x272727)]
    }
    NSGradient(colors: gradientColors)!.draw(in: drop, angle: -90)
    NSGraphicsContext.current?.restoreGraphicsState()

    color(0xFFFFFF, alpha: 0.72).setFill()
    NSBezierPath(ovalIn: NSRect(x: 390, y: 542, width: 105, height: 150)).fill()
    color(0xFFFFFF, alpha: 0.38).setFill()
    NSBezierPath(ovalIn: NSRect(x: 270, y: 742, width: 82, height: 82)).fill()
    NSBezierPath(ovalIn: NSRect(x: 708, y: 700, width: 62, height: 62)).fill()
    color(0xFFFFFF, alpha: 0.28).setFill()
    NSBezierPath(ovalIn: NSRect(x: 210, y: 585, width: 48, height: 48)).fill()

}

func writePNG(appearance: IconAppearance, name: String) throws {
    guard let context = CGContext(
        data: nil,
        width: 1024,
        height: 1024,
        bitsPerComponent: 8,
        bytesPerRow: 1024 * 4,
        space: CGColorSpaceCreateDeviceRGB(),
        bitmapInfo: CGImageAlphaInfo.noneSkipLast.rawValue
    ) else {
        throw CocoaError(.fileWriteUnknown)
    }
    NSGraphicsContext.saveGraphicsState()
    NSGraphicsContext.current = NSGraphicsContext(cgContext: context, flipped: false)
    drawIcon(appearance: appearance)
    NSGraphicsContext.current?.flushGraphics()
    NSGraphicsContext.restoreGraphicsState()
    guard let cgImage = context.makeImage() else {
        throw CocoaError(.fileWriteUnknown)
    }
    let bitmap = NSBitmapImageRep(cgImage: cgImage)
    guard let png = bitmap.representation(using: .png, properties: [:]) else {
        throw CocoaError(.fileWriteUnknown)
    }
    try png.write(to: outputDirectory.appendingPathComponent(name), options: .atomic)
}

try FileManager.default.createDirectory(at: outputDirectory, withIntermediateDirectories: true)
try writePNG(appearance: .light, name: "AppIcon.png")
try writePNG(appearance: .dark, name: "AppIcon-Dark.png")
try writePNG(appearance: .tinted, name: "AppIcon-Tinted.png")
