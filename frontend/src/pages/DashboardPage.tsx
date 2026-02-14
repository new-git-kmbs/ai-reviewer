import { useNavigate } from "react-router-dom";

export default function DashboardPage() {
  const navigate = useNavigate();

  return (
    <div>
      <h2 style={{ marginBottom: 6 }}>Dashboard</h2>
      <div style={{ opacity: 0.75, marginBottom: 18 }}>Choose a module</div>

      <div
        style={{
          display: "grid",
          gridTemplateColumns: "repeat(auto-fit, minmax(260px, 1fr))",
          gap: 14,
        }}
      >
        {/* Product Suite (parent card) */}
        <div
          onClick={() => navigate("/pm")}
          style={{
            border: "1px solid #e5e7eb",
            borderRadius: 14,
            padding: 16,
            cursor: "pointer",
            boxShadow: "0 1px 2px rgba(0,0,0,0.05)",
          }}
        >
          <div style={{ fontSize: 18, fontWeight: 800, marginBottom: 10 }}>
            Product Suite
          </div>

          {/* AI Reviewer (child card) */}
          <div
            onClick={(e) => {
              e.stopPropagation(); // ✅ prevents also triggering the parent (/pm)
              navigate("/ai-reviewer");
            }}
            style={{
              border: "1px solid #e5e5e5",
              borderRadius: 10,
              padding: 14,
              cursor: "pointer",
              transition: "all 0.15s ease",
            }}
          >
            <div style={{ fontWeight: 700, fontSize: 15, marginBottom: 6 }}>
              AI User Story Reviewer
            </div>

            <div style={{ opacity: 0.8, lineHeight: 1.35 }}>
              Score user stories, detect gaps &amp; edge cases, generate Gherkin
              ACs, and produce Jira-ready review comments.
            </div>
          </div>

          <div style={{ marginTop: 12, fontSize: 13, opacity: 0.65 }}>
            Coming soon: AI User Story Generator · AI PRD Builder · AI Backlog
            Optimizer
          </div>
        </div>

        {/* Investing (future) */}
        <div
          style={{
            border: "1px dashed #d1d5db",
            borderRadius: 14,
            padding: 16,
            opacity: 0.7,
          }}
        >
          <div style={{ fontSize: 18, fontWeight: 800, marginBottom: 6 }}>
            Investing
          </div>
          <div>Portfolio calculator, fund comparisons, reverse calculator (future)</div>
        </div>

        {/* Trading (future) */}
        <div
          style={{
            border: "1px dashed #d1d5db",
            borderRadius: 14,
            padding: 16,
            opacity: 0.7,
          }}
        >
          <div style={{ fontSize: 18, fontWeight: 800, marginBottom: 6 }}>
            Trading
          </div>
          <div>Journal, checklist, review tools (future)</div>
        </div>
      </div>
    </div>
  );
}
