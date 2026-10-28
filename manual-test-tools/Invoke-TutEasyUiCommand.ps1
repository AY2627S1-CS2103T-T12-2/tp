param(
    [Parameter(Mandatory = $true)][int] $ProcessId,
    [Parameter(Mandatory = $true)][string] $Command,
    [Parameter(Mandatory = $true)][string] $Label,
    [Parameter(Mandatory = $true)][string] $EvidencePath,
    [int] $WaitMilliseconds = 1200
)

Add-Type -AssemblyName UIAutomationClient
Add-Type -AssemblyName UIAutomationTypes
Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class TutEasyUiCommandInput {
    [DllImport("user32.dll", SetLastError=true)]
    public static extern bool PostMessage(IntPtr hWnd, uint msg, IntPtr wParam, IntPtr lParam);
    public static void PressEnter(IntPtr hWnd) {
        PostMessage(hWnd, 0x0100, new IntPtr(0x0D), IntPtr.Zero);
        PostMessage(hWnd, 0x0101, new IntPtr(0x0D), IntPtr.Zero);
    }
}
"@

$app = Get-Process -Id $ProcessId -ErrorAction Stop
$handle = $app.MainWindowHandle
$root = [System.Windows.Automation.AutomationElement]::RootElement
$windowCondition = New-Object System.Windows.Automation.PropertyCondition(
    [System.Windows.Automation.AutomationElement]::NativeWindowHandleProperty, [int]$handle)
$window = $root.FindFirst([System.Windows.Automation.TreeScope]::Children, $windowCondition)
if ($null -eq $window) { throw "TutEasy window not found for PID $ProcessId" }
$editCondition = New-Object System.Windows.Automation.PropertyCondition(
    [System.Windows.Automation.AutomationElement]::ControlTypeProperty,
    [System.Windows.Automation.ControlType]::Edit)
$edits = $window.FindAll([System.Windows.Automation.TreeScope]::Descendants, $editCondition)
$commandBox = $null
foreach ($edit in $edits) {
    if ($edit.Current.AutomationId -eq 'JavaFX22') { $commandBox = $edit; break }
}
if ($null -eq $commandBox) { throw 'TutEasy command box not found' }
$valuePattern = $null
if (-not $commandBox.TryGetCurrentPattern([System.Windows.Automation.ValuePattern]::Pattern, [ref]$valuePattern)) {
    throw 'TutEasy command box does not expose ValuePattern'
}
$valuePattern.SetValue($Command)
$commandBox.SetFocus()
[TutEasyUiCommandInput]::PressEnter($handle)
Start-Sleep -Milliseconds $WaitMilliseconds
$all = $window.FindAll([System.Windows.Automation.TreeScope]::Descendants,
    [System.Windows.Automation.Condition]::TrueCondition)
$result = ''
$screenText = [System.Collections.Generic.List[string]]::new()
foreach ($element in $all) {
    if ($element.Current.AutomationId -eq 'JavaFX29') {
        $resultPattern = $null
        if ($element.TryGetCurrentPattern([System.Windows.Automation.ValuePattern]::Pattern, [ref]$resultPattern)) {
            $result = $resultPattern.Current.Value
        }
    }
    if ($element.Current.ControlType -eq [System.Windows.Automation.ControlType]::Text -and $element.Current.Name) {
        $screenText.Add($element.Current.Name)
    }
}
New-Item -ItemType Directory -Path $EvidencePath -Force | Out-Null
$path = Join-Path $EvidencePath ($Label + '.txt')
@(
    "Timestamp: $([DateTimeOffset]::Now.ToString('o'))",
    "Test: $Label",
    "Command: $Command",
    "Result display: $result",
    'Visible UI text:',
    ($screenText -join [Environment]::NewLine)
) | Set-Content -LiteralPath $path -Encoding utf8
[pscustomobject]@{ Label=$Label; Command=$Command; Result=$result; Evidence=$path }
