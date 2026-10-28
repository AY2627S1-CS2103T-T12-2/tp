param(
    [Parameter(Mandatory = $true)][int] $ProcessId,
    [Parameter(Mandatory = $true)][string] $Command,
    [Parameter(Mandatory = $true)][string] $EvidencePath,
    [Parameter(Mandatory = $true)][string] $Label,
    [int] $WaitMilliseconds = 1200
)

Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class TutEasyTestInput {
    [StructLayout(LayoutKind.Sequential)] public struct RECT {
        public int Left; public int Top; public int Right; public int Bottom;
    }
    [DllImport("user32.dll")] public static extern bool GetWindowRect(IntPtr hWnd, out RECT rect);
    [DllImport("user32.dll")] public static extern IntPtr SetThreadDpiAwarenessContext(IntPtr context);
    [DllImport("user32.dll")] public static extern bool SetForegroundWindow(IntPtr hWnd);
    [DllImport("user32.dll")] public static extern bool ShowWindow(IntPtr hWnd, int command);
    [DllImport("user32.dll")] public static extern bool SetCursorPos(int x, int y);
    [DllImport("user32.dll")] public static extern void mouse_event(uint flags, uint dx, uint dy, uint data, UIntPtr extra);
    [DllImport("user32.dll")] public static extern void keybd_event(byte key, byte scan, uint flags, UIntPtr extra);
    public static void PasteAndEnter() {
        keybd_event(0x10, 0, 0x0002, UIntPtr.Zero);
        keybd_event(0x11, 0, 0x0002, UIntPtr.Zero);
        keybd_event(0x12, 0, 0x0002, UIntPtr.Zero);
        keybd_event(0x11, 0, 0, UIntPtr.Zero); keybd_event(0x41, 0, 0, UIntPtr.Zero);
        keybd_event(0x41, 0, 0x0002, UIntPtr.Zero); keybd_event(0x11, 0, 0x0002, UIntPtr.Zero);
        keybd_event(0x11, 0, 0, UIntPtr.Zero); keybd_event(0x56, 0, 0, UIntPtr.Zero);
        keybd_event(0x56, 0, 0x0002, UIntPtr.Zero); keybd_event(0x11, 0, 0x0002, UIntPtr.Zero);
        keybd_event(0x0D, 0, 0, UIntPtr.Zero); keybd_event(0x0D, 0, 0x0002, UIntPtr.Zero);
    }
}
"@

[TutEasyTestInput]::SetThreadDpiAwarenessContext([IntPtr](-4)) | Out-Null
$app = Get-Process -Id $ProcessId -ErrorAction Stop
[TutEasyTestInput]::ShowWindow($app.MainWindowHandle, 9) | Out-Null
[TutEasyTestInput]::SetForegroundWindow($app.MainWindowHandle) | Out-Null
$shell = New-Object -ComObject WScript.Shell
$shell.AppActivate($app.MainWindowTitle) | Out-Null
Start-Sleep -Milliseconds 200
$bounds = New-Object TutEasyTestInput+RECT
[TutEasyTestInput]::GetWindowRect($app.MainWindowHandle, [ref] $bounds) | Out-Null
if ($Command -ne '__capture__') {
    [TutEasyTestInput]::SetCursorPos($bounds.Left + 220, $bounds.Top + 165) | Out-Null
    [TutEasyTestInput]::mouse_event(2, 0, 0, 0, [UIntPtr]::Zero)
    [TutEasyTestInput]::mouse_event(4, 0, 0, 0, [UIntPtr]::Zero)
    Set-Clipboard -Value $Command
    Start-Sleep -Milliseconds 150
    [TutEasyTestInput]::PasteAndEnter()
    Start-Sleep -Milliseconds $WaitMilliseconds
}
[TutEasyTestInput]::GetWindowRect($app.MainWindowHandle, [ref] $bounds) | Out-Null
$width = $bounds.Right - $bounds.Left
$height = $bounds.Bottom - $bounds.Top
New-Item -ItemType Directory -Path $EvidencePath -Force | Out-Null
$imagePath = Join-Path $EvidencePath (($Label -replace '[^A-Za-z0-9._-]', '_') + '.png')
Add-Type -AssemblyName System.Drawing
$bitmap = New-Object System.Drawing.Bitmap $width, $height
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)
try {
    $graphics.CopyFromScreen($bounds.Left, $bounds.Top, 0, 0, $bitmap.Size)
    $bitmap.Save($imagePath, [System.Drawing.Imaging.ImageFormat]::Png)
} finally { $graphics.Dispose(); $bitmap.Dispose() }
"$([DateTimeOffset]::Now.ToString('o'))`t$Label`t$Command`t$imagePath" |
    Add-Content -Path (Join-Path $EvidencePath 'commands.tsv') -Encoding utf8
$imagePath
