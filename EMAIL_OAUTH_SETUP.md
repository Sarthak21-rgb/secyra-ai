# Email OAuth Integration Setup Guide

This document explains how to set up OAuth authentication for Gmail and Outlook email integration in SECYRA AI.

## Prerequisites

You will need:
- A Google Cloud Console account (for Gmail)
- An Azure Portal account (for Outlook)
- The SHA-256 fingerprint of your app's signing certificate

## Gmail OAuth Setup

### 1. Create Google Cloud Project

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable the Gmail API:
   - Navigate to "APIs & Services" > "Library"
   - Search for "Gmail API"
   - Click "Enable"

### 2. Configure OAuth Consent Screen

1. Navigate to "APIs & Services" > "OAuth consent screen"
2. Choose "External" user type
3. Fill in the required fields:
   - App name: "SECYRA AI"
   - User support email: your email
   - Developer contact information: your email
4. Add the required scopes:
   - `https://www.googleapis.com/auth/gmail.readonly`
   - `https://www.googleapis.com/auth/gmail.send`
5. Save and submit for verification (you can use it in testing mode without verification)

### 3. Create OAuth 2.0 Client ID

1. Navigate to "APIs & Services" > "Credentials"
2. Click "Create Credentials" > "OAuth client ID"
3. Application type: **Android**
4. Package name: `com.secyra.ai`
5. SHA-256 certificate fingerprint:
   - For debug builds, run:
     ```bash
     keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
     ```
   - For release builds, use your actual keystore
6. Click "Create"
7. Copy the **Client ID** (format: `XXXXXXXXXX-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX.apps.googleusercontent.com`)

### 4. Update App Configuration

Open `GmailOAuthManager.kt` and replace the placeholder:

```kotlin
private const val GMAIL_OAUTH_CLIENT_ID = "YOUR_GMAIL_OAUTH_CLIENT_ID.apps.googleusercontent.com"
```

With your actual Client ID.

## Outlook OAuth Setup

### 1. Register App in Azure Portal

1. Go to [Azure Portal](https://portal.azure.com/)
2. Navigate to "Microsoft Entra ID" (formerly Azure Active Directory)
3. Click "App registrations" > "New registration"
4. Fill in:
   - Name: "SECYRA AI"
   - Supported account types: "Accounts in any organizational directory and personal Microsoft accounts"
   - Redirect URI: Android platform with value: `msauth://com.secyra.ai`
5. Click "Register"
6. Copy the **Application (client) ID** and **Directory (tenant) ID**

### 2. Configure API Permissions

1. In your app registration, go to "API permissions" > "Add a permission"
2. Select "Microsoft Graph" > "Delegated permissions"
3. Add the following permissions:
   - `Mail.Read`
   - `Mail.Send`
4. Click "Add permissions"
5. Click "Grant admin consent for [your organization]" (required for production)

### 3. Update App Configuration

Open `OutlookOAuthManager.kt` and replace the placeholders:

```kotlin
private const val OUTLOOK_CLIENT_ID = "YOUR_OUTLOOK_CLIENT_ID"
private const val OUTLOOK_TENANT_ID = "YOUR_OUTLOOK_TENANT_ID"
```

With your actual Client ID and Tenant ID.

### 4. Add MSAL Dependencies (Not Yet Added)

Update `app/build.gradle.kts` to add:

```kotlin
// Microsoft Authentication Library (MSAL)
implementation("com.microsoft.identity.client:msal:5.7.0")

// Microsoft Graph SDK
implementation("com.microsoft.graph:microsoft-graph:6.15.0")
```

## Android Manifest Configuration

The redirect URIs are already configured in `AndroidManifest.xml`:

- Gmail: Handled by Google Sign-In SDK automatically
- Outlook: `msauth://com.secyra.ai`

## Security Considerations

1. **Never commit OAuth credentials** to source control
2. Use environment variables or secure storage for production
3. Keep your keystore and signing keys secure
4. Regularly rotate OAuth client secrets if using server-side flows
5. Enable app verification for Gmail OAuth consent screen

## Testing

### Gmail OAuth Testing

1. Build and install the app
2. Navigate to the Emails screen
3. Click "Add Email Account"
4. Select "Continue with Gmail"
5. Sign in with your Google account
6. Grant permissions for Gmail read/send access
7. Verify that emails load correctly

### Outlook OAuth Testing

1. First add MSAL dependencies to `build.gradle.kts`
2. Update Client ID and Tenant ID in `OutlookOAuthManager.kt`
3. Build and install the app
4. Navigate to the Emails screen
5. Click "Add Email Account"
6. Select "Continue with Outlook"
7. Sign in with your Microsoft account
8. Grant permissions for Microsoft Graph
9. Verify that emails load correctly

## Troubleshooting

### Gmail OAuth Issues

- **"Web traffic is not allowed"**: Add `<uses-permission android:name="android.permission.INTERNET" />` to manifest (already added)
- **"Invalid client_id"**: Check that the Client ID matches exactly
- **"SHA-256 mismatch"**: Ensure you're using the correct signing certificate fingerprint
- **"Scope not granted"**: Check OAuth consent screen configuration

### Outlook OAuth Issues

- **"Client not found"**: Verify Application ID is correct
- **"Redirect URI mismatch"**: Ensure redirect URI matches Azure configuration
- **"Insufficient privileges"**: Grant admin consent for API permissions
- **"MSAL not initialized"**: Ensure MSAL dependencies are added

## Current Implementation Status

- ✅ Gmail OAuth manager implemented
- ✅ Outlook OAuth manager implemented (requires MSAL dependencies)
- ✅ DataStore persistence for account credentials
- ✅ Email repository with provider selection
- ✅ UI for provider selection
- ⏳ Actual OAuth credentials need to be configured
- ⏳ MSAL dependencies need to be added for Outlook
- ⏳ JWT token decoding for email extraction needs improvement
- ⏳ Token refresh logic needs implementation
- ⏳ Send email functionality needs testing

## Next Steps

1. Configure Gmail OAuth Client ID in `GmailOAuthManager.kt`
2. Test Gmail OAuth flow
3. Add MSAL dependencies for Outlook support
4. Configure Outlook OAuth Client ID and Tenant ID
5. Test Outlook OAuth flow
6. Implement proper JWT token decoding
7. Add token refresh logic
8. Test email sending functionality
