param(
    [string]$ServerAddr = "http://127.0.0.1:8848",
    [string]$Group = "DEFAULT_GROUP"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$configDir = Join-Path $scriptDir "configs"

if (-not (Test-Path -LiteralPath $configDir)) {
    throw "Config directory not found: $configDir"
}

$configFiles = Get-ChildItem -LiteralPath $configDir -Filter *.yml | Sort-Object Name
if ($configFiles.Count -eq 0) {
    throw "No Nacos config files were found under $configDir"
}

$client = [System.Net.Http.HttpClient]::new()
try {
    foreach ($file in $configFiles) {
        $fileContent = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8
        $nonAsciiMatch = [regex]::Match($fileContent, '[^\u0000-\u007F]')
        if ($nonAsciiMatch.Success) {
            throw "Nacos config $($file.Name) contains non-ASCII content starting with '$($nonAsciiMatch.Value)'. Keep Nacos-published YAML ASCII-only because Spring Cloud Alibaba 2023.0.3.2 re-encodes config strings with the Windows default charset before YAML parsing."
        }

        $pairs = New-Object 'System.Collections.Generic.List[System.Collections.Generic.KeyValuePair[string,string]]'
        $pairs.Add([System.Collections.Generic.KeyValuePair[string,string]]::new("dataId", $file.Name))
        $pairs.Add([System.Collections.Generic.KeyValuePair[string,string]]::new("group", $Group))
        $pairs.Add([System.Collections.Generic.KeyValuePair[string,string]]::new("type", "yaml"))
        $pairs.Add([System.Collections.Generic.KeyValuePair[string,string]]::new("content", $fileContent))

        $body = [System.Net.Http.FormUrlEncodedContent]::new($pairs)
        $response = $client.PostAsync("$ServerAddr/nacos/v1/cs/configs", $body).GetAwaiter().GetResult()
        if (-not $response.IsSuccessStatusCode) {
            $responseText = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
            throw "Failed to import $($file.Name): $($response.StatusCode) $responseText"
        }

        Write-Host "Imported Nacos config: $($file.Name)"
    }
}
finally {
    $client.Dispose()
}

Write-Host "Nacos config import completed."
