# Secyra AI - Complete Integration Guide

This document explains what credentials and configurations are needed to make all 4 implemented stages fully functional.

---

## Stage 1: Incoming Call Notification System ✅ IMPLEMENTED

**Status:** Code complete, requires Android system setup

### What's Working:
- ✅ Call notification manager with Answer/Decline/Message actions
- ✅ CallStyle notifications for Android 8.0+
- ✅ Full-screen intent support
- ✅ Notification receiver for action handling
- ✅ Integration with InCallService

### What's Required for Full Functionality:

#### 1. Default Phone App Role (Critical)
**Required:** User must set Secyra as the default phone app

**How to Configure:**
1. User opens the Default Dialer Onboarding screen (already implemented)
2. User taps "Set as Default Phone App"
3. Android shows system dialog to choose default phone app
4. User selects Secyra AI
5. Grant ANSWER_PHONE_CALLS permission when prompted

**Without this:**
- Notifications will appear but Answer/Decline actions may not work
- Full-screen incoming call UI may not show
- InCallService won't receive call events

#### 2. Android Permissions
**Already in manifest:** ✅
- `ANSWER_PHONE_CALLS` - For answering/declining calls
- `READ_PHONE_STATE` - For call state
- `READ_CALL_LOG` - For call history
- `CALL_PHONE` - For placing calls
- `POST_NOTIFICATIONS` - For notifications

**Required Action:** User must grant these when prompted by Android

---

## Stage 2: Default Dialer Onboarding ✅ IMPLEMENTED

**Status:** Code complete, functional

### What's Working:
- ✅ Onboarding screen with benefits explanation
- ✅ Role request through Android RoleManager
- ✅ Permission request for ANSWER_PHONE_CALLS
- ✅ Status checking
- ✅ Daily banner until user sets default
- ✅ Permanent dismissal after user sets default

### What's Required:
**Nothing additional** - This stage is fully functional and requires only user action to complete onboarding.

---

## Stage 3: Threat Intelligence Provider Architecture ✅ IMPLEMENTED

**Status:** Architecture complete, requires API credentials

### What's Working:
- ✅ Provider abstraction layer
- ✅ PlaceholderThreatProvider (returns "unknown" results)
- ✅ ThreatIntelligenceService coordinator
- ✅ Structured result objects
- ✅ Complete integration documentation

### What's Required for Real Threat Detection:

#### Option A: Google Safe Browsing (URL Threat Detection)
**Recommended for:** URL and link scanning

**Required Credentials:**
1. Google Cloud API Key with Safe Browsing API enabled

**How to Get:**
1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing
3. Enable "Safe Browsing API"
4. Create API credentials (API Key)
5. Copy the API key

**Configuration Steps:**
1. Create Supabase Edge Function: `supabase/functions/check-url/index.ts`
2. Add environment variable in Supabase Dashboard:
   - `GOOGLE_SAFE_BROWSING_API_KEY=your_api_key_here`
3. Implement `GoogleSafeBrowsingProvider.kt` class
4. Update `AppModule.kt` to use `GoogleSafeBrowsingProvider` instead of `PlaceholderThreatProvider`

**Cost:** Free tier: 10,000 queries/day

---

#### Option B: Phone Reputation Service
**Recommended for:** Spam detection and caller ID

**Option B1: Truecaller**
**Required:** Truecaller API access (requires approval)

**How to Get:**
1. Apply at [Truecaller Developer Portal](https://developer.truecaller.com)
2. Submit application for approval
3. Receive API credentials upon approval

**Cost:** Commercial pricing (contact Truecaller)

---

**Option B2: Twilio Lookup**
**Required:** Twilio Account and API credentials

**How to Get:**
1. Sign up at [Twilio](https://www.twilio.com)
2. Get Account SID and Auth Token
3. Enable Lookup API

**Cost:** $0.005 per lookup (free tier available)

---

**Option B3: Numverify**
**Required:** Numverify API key

**How to Get:**
1. Sign up at [Numverify](https://numverify.com)
2. Get free API key

**Cost:** Free tier available

**Configuration Steps (for any phone reputation provider):**
1. Create Supabase Edge Function: `supabase/functions/check-phone/index.ts`
2. Add environment variable in Supabase Dashboard:
   - `PHONE_REPUTATION_API_KEY=your_api_key_here`
   - `PHONE_REPUTATION_API_BASE_URL=provider_api_url`
3. Implement `PhoneReputationProvider.kt` class
4. Update `AppModule.kt` to use real provider

---

### Implementation Priority:
1. **High Priority:** Google Safe Browsing (for URL scanning)
2. **Medium Priority:** Phone reputation service (for spam detection)
3. **Low Priority:** Spam reporting (can be added later)

---

## Stage 4: Database & Backend Integration (Not Yet Implemented)

**Status:** Framework ready, requires Supabase setup

### What's Required:

#### 1. Supabase Database Schema
**Required:** Create tables for storing security data

**Tables to Create:**

```sql
-- User security preferences
CREATE TABLE user_security_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    auto_block_spam BOOLEAN DEFAULT false,
    show_call_screening BOOLEAN DEFAULT true,
    notification_settings JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Trusted and blocked numbers
CREATE TABLE phone_numbers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    phone_number TEXT NOT NULL,
    normalized_number TEXT NOT NULL,
    is_trusted BOOLEAN DEFAULT false,
    is_blocked BOOLEAN DEFAULT false,
    category TEXT,
    notes TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, normalized_number)
);

-- User-submitted spam reports
CREATE TABLE spam_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    phone_number TEXT NOT NULL,
    report_reason TEXT,
    provider_report_id TEXT,
    status TEXT DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT NOW()
);

-- Security analysis results
CREATE TABLE security_analyses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    analysis_type TEXT NOT NULL, -- 'url', 'phone', 'message'
    target TEXT NOT NULL, -- URL, phone number, or message ID
    risk_level TEXT, -- 'low', 'medium', 'high', 'unknown'
    confidence FLOAT,
    summary TEXT,
    evidence JSONB,
    recommended_actions JSONB,
    limitations TEXT,
    provider TEXT,
    analyzed_at TIMESTAMP DEFAULT NOW()
);

-- AI usage tracking
CREATE TABLE ai_usage (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id),
    request_type TEXT NOT NULL, -- 'chat', 'analyze'
    tokens_used INTEGER,
    model_used TEXT,
    cost_usd DECIMAL(10, 6),
    created_at TIMESTAMP DEFAULT NOW()
);
```

**How to Create:**
1. Go to Supabase Dashboard → SQL Editor
2. Run the SQL above
3. Enable Row Level Security (RLS) for each table

---

#### 2. Supabase Edge Functions (Additional)
**Required:** Create secure backend functions

**Functions to Create:**

**a. `analyze-url`** - URL threat analysis
- Input: URL
- Output: Security analysis result
- Calls: Google Safe Browsing API

**b. `check-phone`** - Phone reputation check
- Input: Phone number
- Output: Reputation data
- Calls: Chosen phone reputation provider

**c. `report-spam`** - Submit spam report
- Input: Phone number, reason
- Output: Report confirmation
- Calls: Chosen phone reputation provider

**d. `manage-trusted-number`** - Save/block numbers
- Input: Phone number, action (trust/block)
- Output: Confirmation
- Uses: Supabase database

**How to Deploy:**
```bash
npm install -g supabase
supabase login
supabase link --project-ref qzzaqzddxendznpyivot
supabase functions deploy analyze-url
supabase functions deploy check-phone
supabase functions deploy report-spam
supabase functions deploy manage-trusted-number
```

---

#### 3. Environment Variables
**Required:** Configure in Supabase Dashboard → Edge Functions → Environment Variables

**For all functions:**
```
SUPABASE_URL=https://qzzaqzddxendznpyivot.supabase.co/
SUPABASE_ANON_KEY=sb_publishable_6tVfqgas0qqaHZYQ69kAZw_uULBr3kr
```

**For URL analysis function:**
```
GOOGLE_SAFE_BROWSING_API_KEY=your_key_here
```

**For phone reputation function:**
```
PHONE_REPUTATION_API_KEY=your_key_here
PHONE_REPUTATION_API_BASE_URL=provider_url
```

**For Llama functions (already documented in LLAMA_INTEGRATION.md):**
```
LLAMA_API_KEY=your_together_ai_key
LLAMA_API_BASE_URL=https://api.together.xyz/v1
LLM_MODEL_ID=nim/meta/llama-3-1-8b-instruct
MAX_TOKENS=1000
TEMPERATURE=0.7
```

---

#### 4. Row Level Security (RLS) Policies
**Required:** Secure database access

**Example RLS Policies:**
```sql
-- Users can only see their own data
ALTER TABLE user_security_preferences ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Users can view own preferences" 
ON user_security_preferences FOR SELECT 
USING (auth.uid() = user_id);

CREATE POLICY "Users can update own preferences" 
ON user_security_preferences FOR UPDATE 
USING (auth.uid() = user_id);

-- Similar policies for other tables
```

---

## Summary: What You Need to Provide

### 1. Together AI API Key (for Llama integration)
**Where:** Supabase Edge Functions environment variables
**Purpose:** AI chat and security analysis
**Cost:** ~$0.18/1M tokens
**Priority:** High (for AI features)

### 2. Google Safe Browsing API Key (for URL threats)
**Where:** Supabase Edge Functions environment variables
**Purpose:** URL and link scanning
**Cost:** Free tier: 10,000 queries/day
**Priority:** High (for security scanning)

### 3. Phone Reputation API Key (for spam detection)
**Where:** Supabase Edge Functions environment variables
**Purpose:** Phone number reputation and spam detection
**Options:** Truecaller, Twilio, Numverify
**Cost:** Varies by provider
**Priority:** Medium (for caller ID)

### 4. Supabase Database Setup
**Where:** Supabase Dashboard → SQL Editor
**Purpose:** Store user preferences, trusted numbers, spam reports
**Cost:** Free tier available
**Priority:** Medium (for data persistence)

### 5. Edge Function Deployment
**Where:** Terminal with Supabase CLI
**Purpose:** Secure backend for API calls
**Cost:** Free tier: 500K invocations/month
**Priority:** High (for security)

---

## Implementation Order (Recommended)

1. **Immediate:** Set up Together AI key (AI features)
2. **High Priority:** Deploy Llama Edge Functions (AI chat)
3. **High Priority:** Get Google Safe Browsing key (URL scanning)
4. **Medium Priority:** Set up Supabase database (data persistence)
5. **Medium Priority:** Get phone reputation API key (spam detection)
6. **Low Priority:** Implement provider classes in Kotlin
7. **Low Priority:** Deploy additional Edge Functions

---

## Testing Checklist

After providing credentials:

- [ ] Test AI chat with real Together API
- [ ] Test URL scanning with Google Safe Browsing
- [ ] Test phone reputation with chosen provider
- [ ] Test database operations (save trusted numbers)
- [ ] Test spam reporting
- [ ] Test default phone app role on real device
- [ ] Test incoming call notification on real device
- [ ] Test Answer/Decline actions on real device
- [ ] Test timestamp formatting in different timezones
- [ ] Test call grouping with real call history

---

## Cost Estimates (Monthly)

- **Together AI:** ~$0.10-1.00 (depends on usage)
- **Google Safe Browsing:** Free (up to 10K queries/day)
- **Phone Reputation:** ~$0.01-0.10 (depends on provider and usage)
- **Supabase:** Free (up to 500K Edge Function invocations/month, 500MB database)
- **Total Estimated:** ~$0.11-1.11/month for moderate usage

---

## Next Steps

1. **Get Together AI API key** from https://together.ai
2. **Configure in Supabase** Edge Functions environment variables
3. **Deploy Llama Edge Functions** using Supabase CLI
4. **Test AI chat** in the app
5. **Get Google Safe Browsing API key** from Google Cloud Console
6. **Create Supabase database** using the SQL schema above
7. **Implement real provider classes** in Kotlin
8. **Deploy additional Edge Functions** for threat intelligence
9. **Test all integrations** with real data
10. **Monitor usage** and adjust as needed
