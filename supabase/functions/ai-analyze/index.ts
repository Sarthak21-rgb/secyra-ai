import { serve } from "https://deno.land/std@0.224.0/http/server.ts";

// Together AI configuration
const TOGETHER_API_BASE_URL = Deno.env.get("LLAMA_API_BASE_URL") || "https://api.together.xyz/v1";
const TOGETHER_API_KEY = Deno.env.get("LLAMA_API_KEY");
const LLM_MODEL_ID = Deno.env.get("LLM_MODEL_ID") || "nim/meta/llama-3-1-8b-instruct";
const MAX_TOKENS = parseInt(Deno.env.get("MAX_TOKENS") || "1000");
const TEMPERATURE = parseFloat(Deno.env.get("TEMPERATURE") || "0.7");

if (!TOGETHER_API_KEY) {
  console.error("LLAMA_API_KEY environment variable is not set");
  Deno.exit(1);
}

// Structured analysis prompt template
const ANALYSIS_SYSTEM_PROMPT = `You are Secyra AI Security Analyzer. Analyze the provided content for security risks and return a structured JSON response.

Your response must be valid JSON with this exact structure:
{
  "risk_level": "low|medium|high|unknown",
  "confidence": 0.0-1.0,
  "summary": "Brief explanation of the analysis",
  "evidence": ["list of observed indicators"],
  "recommended_actions": ["list of safe next steps"],
  "limitations": ["missing evidence or uncertainties"]
}

Rules:
- risk_level: Use "unknown" if evidence is insufficient
- confidence: 0.0-1.0, reflect certainty in your assessment
- evidence: List only observed facts, not assumptions
- recommended_actions: Safe, reversible steps user can take
- limitations: Honestly state what you cannot verify

Analysis types:
- url_analysis: Check for phishing, suspicious domains, encoding tricks
- phone_analysis: Check for known scam patterns, spoofing indicators
- message_analysis: Check for scam language, urgency, manipulation tactics
- general_security: General security guidance

Never claim something is 100% safe. If you find no suspicious indicators, state that as a limitation rather than proof of safety.`;

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
    const { content, analysis_type, context } = await req.json();

    // Build context-aware prompt
    const userPrompt = buildAnalysisPrompt(content, analysis_type, context);

    const togetherResponse = await fetch(`${TOGETHER_API_BASE_URL}/chat/completions`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${TOGETHER_API_KEY}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        model: LLM_MODEL_ID,
        messages: [
          { role: "system", content: ANALYSIS_SYSTEM_PROMPT },
          { role: "user", content: userPrompt }
        ],
        max_tokens: MAX_TOKENS,
        temperature: TEMPERATURE,
        stream: false,
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
    const aiContent = data.choices[0]?.message?.content;

    if (!aiContent) {
      return new Response(
        JSON.stringify({ error: "Empty response from AI" }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Parse the JSON response
    let structuredResponse;
    try {
      // Extract JSON from the response (in case the model adds extra text)
      const jsonMatch = aiContent.match(/\{[\s\S]*\}/);
      const jsonString = jsonMatch ? jsonMatch[0] : aiContent;
      structuredResponse = JSON.parse(jsonString);
    } catch (parseError) {
      console.error("Failed to parse AI response as JSON:", parseError);
      return new Response(
        JSON.stringify({ error: "Invalid response format from AI", raw: aiContent }),
        { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
      );
    }

    // Validate response structure
    const requiredFields = ["risk_level", "confidence", "summary", "evidence", "recommended_actions", "limitations"];
    for (const field of requiredFields) {
      if (!(field in structuredResponse)) {
        return new Response(
          JSON.stringify({ error: `Missing required field: ${field}` }),
          { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } }
        );
      }
    }

    return new Response(JSON.stringify(structuredResponse), {
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

function buildAnalysisPrompt(content: string, analysisType: string, context: Record<string, string>): string {
  let prompt = `Analysis Type: ${analysisType}\n\nContent to analyze:\n${content}\n\n`;

  if (context.url) {
    prompt += `URL: ${context.url}\n`;
  }
  if (context.phone) {
    prompt += `Phone Number: ${context.phone}\n`;
  }
  if (context.message) {
    prompt += `Message: ${context.message}\n`;
  }

  prompt += `\nProvide a structured JSON response following the system prompt requirements.`;
  return prompt;
}
