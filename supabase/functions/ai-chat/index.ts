import { serve } from "https://deno.land/std@0.224.0/http/server.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

// Together AI configuration
const TOGETHER_API_BASE_URL = Deno.env.get("LLAMA_API_BASE_URL") || "https://api.together.xyz/v1";
const TOGETHER_API_KEY = Deno.env.get("LLAMA_API_KEY");
const LLM_MODEL_ID = Deno.env.get("LLM_MODEL_ID") || "nim/meta/llama-3-1-8b-instruct";
const MAX_TOKENS = parseInt(Deno.env.get("MAX_TOKENS") || "1000");
const TEMPERATURE = parseFloat(Deno.env.get("TEMPERPERATURE") || "0.7");

if (!TOGETHER_API_KEY) {
  console.error("LLAMA_API_KEY environment variable is not set");
  Deno.exit(1);
}

// Cybersecurity-focused system prompt
const SYSTEM_PROMPT = `You are Secyra AI, a cybersecurity assistant designed to help users understand security risks and stay safe online.

Your role is to:
1. OBSERVE: Identify security patterns and indicators
2. ANALYZE: Evaluate risks and threats based on evidence
3. EXPLAIN: Make complex security concepts understandable
4. RECOMMEND: Suggest safe next steps
5. REMEMBER: The user decides what actions to take

Important rules:
- Never request passwords, OTPs, recovery codes, or private keys
- Do not automatically block callers, delete messages, or change settings
- Require user confirmation for sensitive actions
- Distinguish between observed facts and your inferences
- If you're uncertain about a risk level, say so explicitly
- Never claim something is 100% safe if you only found no suspicious indicators
- If evidence is missing, explain what you would need to verify

Focus on:
- Scam and fraud message analysis
- Suspicious call and caller context
- URL and phishing risk explanation
- Social engineering and impersonation detection
- Privacy and mobile security guidance
- Evidence-based security reports

Make your responses clear, concise, and actionable for non-technical users.`;

serve(async (req) => {
  const corsHeaders = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, Authorization",
  };

  if (req.method === "OPTIONS") {
    return new Response(null, { headers: corsHeaders });
  }

  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405, headers: corsHeaders });
  }

  try {
    const { messages, model, max_tokens, temperature, stream } = await req.json();

    // Add system prompt if not present
    const enhancedMessages = messages.some(m => m.role === "system")
      ? messages
      : [{ role: "system", content: SYSTEM_PROMPT }, ...messages];

    const togetherResponse = await fetch(`${TOGETHER_API_BASE_URL}/chat/completions`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${TOGETHER_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: model || LLM_MODEL_ID,
        messages: enhancedMessages,
        max_tokens: max_tokens || MAX_TOKENS,
        temperature: temperature || TEMPERATURE,
        stream: stream || false,
      }),
    });

    if (!togetherResponse.ok) {
      const errorText = await togetherResponse.text();
      console.error("Together AI API error:", errorText);
      return new Response(
        JSON.stringify({ error: "Failed to get AI response", details: errorText }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    const data = await togetherResponse.json();
    return new Response(JSON.stringify(data), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    });
  } catch (error) {
    console.error("Error processing request:", error);
    return new Response(
      JSON.stringify({ error: "Internal server error", details: error.message }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
    );
  }
});
