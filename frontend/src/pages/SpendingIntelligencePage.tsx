import { useState } from "react";
import { useAuth } from "@clerk/clerk-react";

export default function SpendingIntelligencePage() {
  const { getToken } = useAuth();

  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  async function handleUpload() {
    const API_BASE = import.meta.env.VITE_API_BASE_URL as string | undefined;

    if (!file) {
      setMessage("Please select a file first.");
      return;
    }

    if (!API_BASE) {
      setMessage("Missing VITE_API_BASE_URL configuration.");
      return;
    }

    try {
      setLoading(true);
      setMessage(null);

      const token = await getToken();
      if (!token) {
        throw new Error("Authentication failed. Please sign in again.");
      }

      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch(`${API_BASE}/api/transactions/upload`, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${token}`,
        },
        body: formData,
      });

      const text = await response.text();

      if (!response.ok) {
        throw new Error(text || "Upload failed.");
      }

      setMessage("File uploaded successfully. Processing complete.");
    } catch (err: any) {
      setMessage(err?.message ?? "Unexpected error occurred.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div
      style={{
        maxWidth: 900,
        margin: "24px auto",
        fontFamily: "system-ui",
        padding: 12,
      }}
    >
      <h1 style={{ marginBottom: 4 }}>AI Spending Intelligence</h1>

      <div style={{ opacity: 0.8, marginBottom: 20 }}>
        Upload your bank export (CSV or Excel) to receive AI-powered monthly
        summaries, category breakdowns, and savings insights.
      </div>

      {/* Upload Section */}
      <div
        style={{
          border: "1px solid #e5e7eb",
          borderRadius: 12,
          padding: 18,
          marginBottom: 20,
        }}
      >
        <div style={{ fontWeight: 700, marginBottom: 10 }}>
          Upload Transactions File
        </div>

        <input
          type="file"
          accept=".csv,.xlsx,.xls"
          onChange={(e) => {
            if (e.target.files && e.target.files.length > 0) {
              setFile(e.target.files[0]);
            }
          }}
          style={{ marginBottom: 12 }}
        />

        <div>
          <button
            onClick={handleUpload}
            disabled={loading}
            style={{
              padding: "8px 14px",
              cursor: loading ? "not-allowed" : "pointer",
            }}
          >
            {loading ? "Uploading..." : "Upload & Analyze"}
          </button>
        </div>

        {message && (
          <div
            style={{
              marginTop: 12,
              color: message.includes("success") ? "green" : "crimson",
            }}
          >
            {message}
          </div>
        )}
      </div>

      {/* Placeholder Summary Section */}
      <div
        style={{
          border: "1px solid #eee",
          borderRadius: 12,
          padding: 18,
          opacity: 0.7,
        }}
      >
        <div style={{ fontWeight: 700, marginBottom: 8 }}>
          Monthly Summary (Coming Soon)
        </div>

        <div>
          After upload, you will see:
          <ul style={{ marginTop: 8 }}>
            <li>Total spending</li>
            <li>Category breakdown</li>
            <li>Top merchants</li>
            <li>Month-over-month comparison</li>
            <li>AI-generated insights & recommendations</li>
          </ul>
        </div>
      </div>
    </div>
  );
}
