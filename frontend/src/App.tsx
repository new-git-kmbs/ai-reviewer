import {
  SignedIn,
  SignedOut,
  SignInButton,
  UserButton,
  RedirectToSignIn,
} from "@clerk/clerk-react";
import { BrowserRouter, Routes, Route, Navigate, Link } from "react-router-dom";
import DashboardPage from "./pages/DashboardPage";
import AiReviewerPage from "./pages/AiReviewerPage";

export default function App() {
  return (
    <BrowserRouter>
      {/* ✅ Outer wrapper forces "normal page" layout even if body/#root are flex-centered */}
      <div
        style={{
          minHeight: "100vh",
          width: "100%",
          display: "block",
          paddingTop: 8,
        }}
      >
        {/* ✅ Your app container */}
        <div
          style={{
            maxWidth: 1100,
            margin: "0 auto",
            padding: 12,
            fontFamily: "system-ui",
          }}
        >
          {/* Top bar */}
          <div
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              marginBottom: 36,
            }}
          >
            <Link to="/dashboard" style={{ textDecoration: "none", color: "inherit" }}>
              <div style={{ display: "flex", flexDirection: "column", lineHeight: 1.2 }}>
                <span style={{ fontWeight: 800, fontSize: 22 }}>Peruri Labs</span>
                <span style={{ fontSize: 12, opacity: 0.6 }}>Founded by Vidya Peruri</span>
              </div>
            </Link>

            <div>
              <SignedOut>
                <SignInButton mode="modal">
                  <button style={{ padding: "8px 12px" }}>Sign In</button>
                </SignInButton>
              </SignedOut>

              <SignedIn>
                <UserButton afterSignOutUrl="/dashboard" />
              </SignedIn>
            </div>
          </div>

          {/* Routes */}
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* Public route */}
            <Route path="/dashboard" element={<DashboardPage />} />

            {/* Protected route */}
            <Route
              path="/ai-reviewer"
              element={
                <>
                  <SignedIn>
                    <AiReviewerPage />
                  </SignedIn>
                  <SignedOut>
                    <RedirectToSignIn />
                  </SignedOut>
                </>
              }
            />

            {/* Fallback */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </div>
    </BrowserRouter>
  );
}
