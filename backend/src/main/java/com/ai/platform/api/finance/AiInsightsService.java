package com.ai.platform.api.finance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiInsightsService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    public Map<String, Object> generateInsights(Map<String, Object> summary) {

        try {
            String apiKey = System.getenv("ANTHROPIC_API_KEY");
            if (apiKey == null || apiKey.isBlank())
                throw new IllegalStateException("Missing ANTHROPIC_API_KEY");

            String model = System.getenv("ANTHROPIC_MODEL");
            if (model == null || model.isBlank())
                throw new IllegalStateException("Missing ANTHROPIC_MODEL");

            System.out.println("Using Anthropic model (insights): " + model);

            String json = objectMapper.writeValueAsString(summary);

String prompt = """
You are a personal financial coach analyzing a single month's spending.

Your goal is NOT to summarize the report.
Your goal is to identify where money is going, what is controllable, and how the user could realistically save more.

Interpretation rules:
- Only treat categories representing actual spending as spending behavior.
- Ignore refunds, payroll, transfers, bill payments, and investments when determining spending patterns.
- Refunds represent returned money and should not influence top categories or merchants.
- Transfers and bill payments represent movement of money between accounts, not spending.

Spending classification rules:

- Only use categories representing actual spending behavior.
- Ignore the category named "Refunds".
- Ignore any incoming money such as Zelle payments, reimbursements, or refunds.
- Ignore payroll, transfers, bill payments, and investments when identifying top spending categories or merchants.
- Top category and top merchant MUST come from spending categories only.

Merchant rules:
- Focus on merchants where money was actually spent (restaurants, stores, services).
- Do not treat financial institutions, transfers, or payment processors as merchants.

Focus on:
- Largest spending drivers
- Fixed vs discretionary spending
- High-frequency small transactions
- Concentration in specific merchants
- Clear, quantified savings opportunities
- Behavioral patterns (habits)
- Any unusual spikes or irregularities

When analyzing habits:
- Identify repeat merchants that indicate behavioral spending patterns.
- Highlight subscriptions or recurring charges if present.
- Distinguish between lifestyle spending and essential bills.

Avoid:
- Repeating totals already visible in the report
- Describing categories mechanically
- Overemphasizing transfers or internal payments
- Treating refunds or incoming payments as spending
- Generic advice like "review your spending"

When suggesting savings, quantify impact if possible (e.g., reducing dining frequency by 25% could save approximately $X per month).

Return STRICT JSON only (no markdown):

{
  "highlights": [string],
  "topSpendingCategory": string,
  "topMerchant": string,
  "concentrationNotes": [string],
  "optimizationIdeas": [string],
  "anomalies": [string]
}

Summary:
""" + json;

            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", 600,
                    "temperature", 0.3,
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            Map<String, Object> resp = restClient.post()
                    .uri("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("content-type", "application/json")
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (resp == null || !resp.containsKey("content"))
                throw new IllegalStateException("Invalid Anthropic response");

            List<?> content = (List<?>) resp.get("content");
            Map<?, ?> first = (Map<?, ?>) content.get(0);

            String raw = String.valueOf(first.get("text")).trim();

            // Remove markdown fences if Claude adds them
            if (raw.startsWith("```")) {
                raw = raw.replaceAll("^```[a-zA-Z]*\\s*", "");
                raw = raw.replaceAll("\\s*```$", "");
                raw = raw.trim();
            }

            return objectMapper.readValue(
                    raw,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("AI insights failed", e);
        }
    }
}