# Threat Intelligence Integration

This document explains how to integrate real threat intelligence providers into Secyra AI.

## Current Status

**Placeholder Implementation**: The app currently uses a placeholder threat intelligence provider that returns "unknown" results. This demonstrates the architecture but does not provide real threat detection.

## Required Integrations

### 1. URL Threat Detection

**Recommended Provider**: Google Safe Browsing API

**Benefits**:
- Free for non-commercial use
- Comprehensive URL database
- Real-time threat detection
- Google's curated threat intelligence

**Setup Steps**:
1. Get Google Safe Browsing API key from [Google Cloud Console](https://console.cloud.google.com)
2. Create a Supabase Edge Function to proxy requests (protect API key)
3. Implement SafeBrowsingProvider implementing ThreatIntelligenceProvider
4. Configure API key in Supabase Edge Function environment variables

**API Documentation**: https://developers.google.com/safe-browsing/v4

### 2. Phone Number Reputation

**Recommended Providers**:
- Truecaller API (commercial, requires approval)
- Twilio Lookup API (paid)
- Numverify API (free tier available)

**Benefits**:
- Spam detection
- Caller ID verification
- Reputation scoring
- Report aggregation

**Setup Steps**:
1. Register with chosen provider
2. Get API credentials
3. Create Supabase Edge Function for secure API calls
4. Implement PhoneReputationProvider
5. Configure credentials in Supabase

### 3. Spam Reporting

**Implementation**:
- Many phone reputation providers allow spam reporting
- Implement reportSpam() method in provider
- Track user reports locally
- Sync with provider when available

## Implementation Guide

### Step 1: Create Provider Implementation

```kotlin
@Singleton
class GoogleSafeBrowsingProvider @Inject constructor(
    private val supabaseConfig: SupabaseConfig
) : ThreatIntelligenceProvider {
    
    override val providerName: String = "Google Safe Browsing"
    override val isConfigured: Boolean = true // Check if API key is configured
    
    override suspend fun checkUrl(url: String): UrlThreatResult {
        // Call Supabase Edge Function
        // Edge Function calls Google Safe Browsing API
        // Return structured result
    }
    
    // ... other methods
}
```

### Step 2: Create Supabase Edge Function

```typescript
// supabase/functions/check-url/index.ts
import { serve } from 'https://deno.land/std@0.168.0/http/server.ts'

serve(async (req) => {
  const { url } = await req.json()
  
  // Call Google Safe Browsing API
  const response = await fetch('https://safebrowsing.googleapis.com/v4/threatMatches:find', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      client: {
        clientId: 'secyra-ai',
        clientVersion: '1.0.0'
      },
      threatInfo: {
        threatTypes: ['MALWARE', 'SOCIAL_ENGINEERING'],
        platformTypes: ['ANY_PLATFORM'],
        threatEntryTypes: ['URL'],
        threatEntries: [{ url }]
      }
    })
  })
  
  const data = await response.json()
  return new Response(JSON.stringify(data), {
    headers: { 'Content-Type': 'application/json' }
  })
})
```

### Step 3: Configure Environment Variables

In Supabase Dashboard → Edge Functions → Environment Variables:

```
GOOGLE_SAFE_BROWSING_API_KEY=your_api_key_here
```

### Step 4: Update AppModule

```kotlin
@Provides
@Singleton
fun provideThreatIntelligenceProvider(
    supabaseConfig: SupabaseConfig
): ThreatIntelligenceProvider {
    return GoogleSafeBrowsingProvider(supabaseConfig)
}
```

## Cost Estimates

### Google Safe Browsing
- Free tier: 10,000 queries/day
- Paid tier: $5 per 1,000,000 queries

### Truecaller
- Commercial pricing (contact for details)
- Requires approval for access

### Twilio Lookup
- $0.005 per lookup
- Free tier available for testing

## Privacy Considerations

- Never upload private user data without consent
- Use server-side API calls to protect keys
- Minimize data sent to providers
- Comply with provider terms of service
- Respect user privacy settings

## Testing

Test the integration with:
1. Known malicious URLs
2. Known safe URLs
3. Phone numbers with spam reports
4. Phone numbers without reports
5. Error handling for API failures

## Security

- Store API keys in Supabase environment variables
- Never expose keys in Android app
- Use Supabase authentication where applicable
- Implement rate limiting
- Monitor API usage

## Next Steps

1. Choose URL threat detection provider
2. Choose phone reputation provider
3. Get API credentials
4. Implement provider classes
5. Create Supabase Edge Functions
6. Configure environment variables
7. Test with real data
8. Update documentation
