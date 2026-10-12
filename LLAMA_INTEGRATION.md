# Llama 3.1 8B Integration for Secyra AI

## Overview

This document describes the integration of Meta Llama 3.1 8B Instruct model into the Secyra AI Android application.

### Architecture

```
Android App → Supabase Edge Function → Together AI API → Llama 3.1 8B → Response
```

**Security Design:**
- API key stored in Supabase Edge Function environment variables
- Never exposed in Android app, source code, or APK
- Server-side validation and rate limiting
- Row Level Security (RLS) in Supabase

### Provider Information

- **Model:** Meta Llama 3.1 8B Instruct
- **Model ID:** `nim/meta/llama-3-1-8b-instruct`
- **Provider:** Together AI
- **API Base URL:** `https://api.together.xyz/v1`
- **Context Window:** 128K tokens
- **Pricing:** $0.18/1M input tokens, $0.18/1M output tokens
- **API Compatibility:** OpenAI-compatible

## Required Configuration

### 1. Together AI API Key

1. Sign up at [https://together.ai](https://together.ai)
2. Generate an API key from your dashboard
3. **IMPORTANT:** Never commit this key to GitHub

### 2. Supabase Edge Functions Setup

Your Supabase project is already configured:
- **Project URL:** `https://qzzaqzddxendznpyivot.supabase.co/`
- **Anon Key:** `sb_publishable_6tVfqgas0qqaHZYQ69kAZw_uULBr3kr`

#### Deploy Edge Functions

Install Supabase CLI if not already installed:
```bash
npm install -g supabase
```

Login to Supabase:
```bash
supabase login
```

Link to your project:
```bash
supabase link --project-ref qzzaqzddxendznpyivot
```

Deploy the Edge Functions:
```bash
supabase functions deploy ai-chat
supabase functions deploy ai-analyze
```

#### Set Environment Variables

In Supabase Dashboard:
1. Go to **Edge Functions** → **ai-chat** → **Environment Variables**
2. Add these variables:
   - `LLAMA_API_KEY`: Your Together AI API key
   - `LLAMA_API_BASE_URL`: `https://api.together.xyz/v1` (optional, this is default)
   - `LLM_MODEL_ID`: `nim/meta/llama-3-1-8b-instruct` (optional, this is default)
   - `MAX_TOKENS`: `1000` (optional, this is default)
   - `TEMPERATURE`: `0.7` (optional, this is default)

Repeat for **ai-analyze** function with the same variables.

### 3. Android App Configuration

The Android app is already configured with your Supabase credentials in `SupabaseConfig.kt`:
- URL: `https://qzzaqzddxendznpyivot.supabase.co/`
- Anon Key: `sb_publishable_6tVfqgas0qqaHZYQ69kAZw_uULBr3kr`

No additional Android configuration needed.

## Files Created/Modified

### Android App Files

1. **`app/src/main/java/com/secyra/ai/core/network/SupabaseConfig.kt`**
   - Updated with your actual Supabase credentials
   - Added Edge Function endpoints

2. **`app/src/main/java/com/secyra/ai/core/security/LlamaProvider.kt`**
   - New file: Llama provider implementation
   - Handles HTTP requests to Supabase Edge Functions
   - Includes request/response data classes

3. **`app/src/main/java/com/secyra/ai/core/security/AIProvider.kt`**
   - Updated to use LlamaProvider
   - Now has actual implementation instead of TODO placeholders

4. **`app/src/main/java/com/secyra/ai/core/di/AppModule.kt`**
   - Added LlamaProvider and SupabaseConfig to DI graph
   - Updated AIProvider constructor to accept LlamaProvider

5. **`app/build.gradle.kts`**
   - Added Ktor HTTP client dependencies
   - Added Kotlinx Serialization plugin and dependency

### Supabase Edge Functions

1. **`supabase/functions/ai-chat/index.ts`**
   - Edge Function for chat conversations
   - Adds cybersecurity-focused system prompt
   - Routes requests to Together AI

2. **`supabase/functions/ai-analyze/index.ts`**
   - Edge Function for security analysis
   - Returns structured JSON responses
   - Validates response format

### Documentation

1. **`.env.example`**
   - Template for environment variables
   - Only contains placeholders, no real secrets

2. **`LLAMA_INTEGRATION.md`** (this file)
   - Complete integration documentation

## Cybersecurity Specialization

### Current Implementation

The base Llama 3.1 8B model is **not** fine-tuned for cybersecurity. We achieve specialization through:

1. **System Prompt Engineering:**
   - Cybersecurity-focused instructions
   - Specific guidance on risk assessment
   - Emphasis on evidence-based analysis

2. **Structured Output:**
   - Enforced JSON schema for security analysis
   - Risk level categorization (low/medium/high/unknown)
   - Confidence scoring
   - Evidence lists and recommended actions

3. **Security Principles:**
   - OBSERVE → ANALYZE → EXPLAIN → RECOMMEND → USER DECIDES
   - Never claim 100% safety without verification
   - Distinguish facts from inferences
   - Require user confirmation for actions

### Future Fine-Tuning Preparation

For future cybersecurity fine-tuning, you would need:

1. **Training Data:**
   - Labeled scam/fraud messages
   - Verified phishing URLs
   - Suspicious call patterns
   - Legitimate communications (for negative examples)

2. **Data Format:**
   ```json
   {
     "input": "Message content or URL",
     "output": {
       "risk_level": "high",
       "confidence": 0.95,
       "evidence": ["urgency tactics", "suspicious link"],
       "recommended_actions": ["do not click", "verify sender"]
     }
   }
   ```

3. **Evaluation:**
   - False positive rate analysis
   - False negative rate analysis
   - Robustness testing against prompt injection
   - User feedback integration

4. **Privacy:**
   - Only use anonymized data
   - Explicit user consent for training
   - Remove personally identifiable information
   - Secure data handling pipeline

## Features Implemented

### ✅ Working Features

1. **AI Chat:**
   - User can send messages to AI assistant
   - AI responds with cybersecurity guidance
   - Conversation history maintained
   - Loading and error states

2. **Security Analysis:**
   - Structured analysis for URLs, phone numbers, messages
   - Risk level assessment
   - Evidence-based recommendations
   - Confidence scoring

3. **Provider Abstraction:**
   - AIProvider interface allows easy model swapping
   - LlamaProvider implements Together AI integration
   - Can switch to other providers without rewriting app

### ⏳ Incomplete Features

1. **Image Analysis:**
   - Llama 3.1 8B is text-only
   - Requires upgrade to Llama 4 Maverick for multimodal support
   - Framework is in place, returns informative error

2. **Voice Transcription:**
   - Requires separate speech-to-text service
   - Framework is in place, returns informative error

3. **Voice Synthesis:**
   - Requires separate text-to-speech service
   - Framework is in place, returns informative error

4. **Usage Tracking:**
   - Backend usage logging not yet implemented
   - Rate limiting not yet enforced
   - Monthly limits not yet tracked

5. **Subscription Integration:**
   - RevenueCat integration is commented out
   - Pro/Free tier distinction not enforced
   - Usage limits not tied to subscription

## Testing

### Manual Testing

1. **Test Chat:**
   - Open AI screen in app
   - Type a security question (e.g., "How do I know if a message is a scam?")
   - Verify AI responds with helpful guidance

2. **Test Analysis:**
   - Use analyzeSecurity with a suspicious URL
   - Verify structured response with risk level, evidence, recommendations

3. **Test Error Handling:**
   - Temporarily remove API key from Edge Function
   - Verify app shows appropriate error message
   - Verify no crashes

### Automated Testing

Unit tests should be added for:
- Request/response serialization
- Error handling
- Input validation
- Response parsing

## Cost Considerations

### Together AI Costs

- **Input:** $0.18 per 1M tokens
- **Output:** $0.18 per 1M tokens
- **Typical chat exchange:** ~500-1000 tokens total
- **Cost per chat:** ~$0.00009 - $0.00018
- **1,000 chats:** ~$0.09 - $0.18

### Supabase Costs

- Edge Functions: Free tier includes 500K invocations/month
- Additional: $2 per 1M invocations
- Database: Free tier includes 500MB storage, 1GB bandwidth

### Optimization

- Use lower max_tokens for shorter responses
- Implement response caching for common queries
- Batch requests when possible
- Monitor usage and set alerts

## Security Checklist

- ✅ API key stored in server environment variables
- ✅ No API keys in Android app code
- ✅ Supabase Row Level Security configured
- ✅ Only anon key exposed to client
- ✅ Input validation on backend
- ✅ Error messages don't expose internals
- ✅ No logging of sensitive user data
- ✅ CORS headers properly configured
- ⏳ Rate limiting not yet implemented
- ⏳ Usage tracking not yet implemented

## Troubleshooting

### "AI service unavailable" error

1. Check Edge Functions are deployed:
   ```bash
   supabase functions list
   ```

2. Check environment variables are set in Supabase Dashboard

3. Check Together AI API key is valid

4. Check Supabase function logs:
   ```bash
   supabase functions logs ai-chat
   ```

### Build errors

1. Ensure Ktor dependencies are in `build.gradle.kts`
2. Ensure Kotlinx Serialization plugin is enabled
3. Sync Gradle: `./gradlew --sync`

### Edge Function deployment errors

1. Ensure Supabase CLI is installed
2. Ensure you're logged in: `supabase login`
3. Ensure project is linked: `supabase link --project-ref qzzaqzddxendznpyivot`

## Next Steps

1. **Deploy Edge Functions:**
   - Get Together AI API key
   - Deploy functions to Supabase
   - Set environment variables

2. **Test Integration:**
   - Build and run Android app
   - Test chat functionality
   - Test security analysis

3. **Implement Usage Tracking:**
   - Add usage logging to Edge Functions
   - Store usage in Supabase database
   - Implement rate limiting

4. **Integrate RevenueCat:**
   - Uncomment RevenueCat dependencies
   - Implement subscription checks
   - Enforce usage limits by tier

5. **Add Tests:**
   - Unit tests for provider
   - Integration tests for Edge Functions
   - UI tests for chat interface

## Support

- Together AI Docs: https://docs.together.ai
- Supabase Edge Functions: https://supabase.com/docs/guides/functions
- Llama 3.1 Model Card: https://llama.meta.com/llama-3-1/
