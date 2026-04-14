import React from "react";
import { BrowserRouter, Routes, Route, NavLink, useLocation } from "react-router-dom";
import AddDonor from "./components/AddDonor";
import BloodRequest from "./components/BloodRequest";
import Login from "./components/Login";
import Signup from "./components/Signup";
import Profile from "./components/Profile";
// removed ProtectedRoute - simplified auth flow
import "./index.css";
function Layout() {
  const location = useLocation();
  const [auth, setAuth] = React.useState(null);

  React.useEffect(() => {
    const raw = localStorage.getItem('bm_auth');
    if (raw) setAuth(JSON.parse(raw));
  }, [location.pathname]);

  const logout = () => { localStorage.removeItem('bm_auth'); setAuth(null); window.location.href = '/'; };

  return (
    <div className="app-root">
      <div className="bg-grid" />
      <div className="bg-glow" />

      <header className="app-header">
        <div className="logo-mark">
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <path d="M16 4C16 4 8 12 8 19a8 8 0 0016 0c0-7-8-15-8-15z" fill="url(#drop)" />
            <defs>
              <linearGradient id="drop" x1="8" y1="4" x2="24" y2="28" gradientUnits="userSpaceOnUse">
                <stop stopColor="#60a5fa" />
                <stop offset="1" stopColor="#1d4ed8" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div>
          <h1 className="app-title">BloodLink</h1>
          <p className="app-subtitle">Intelligent Donor Matching System</p>
        </div>
        <div style={{ marginLeft: 'auto', display: 'flex', gap: 12 }}>
          {!auth ? (
            <>
              <button className="btn" onClick={() => window.location.href = '/login'} style={{ width: 'auto', padding: '8px 12px' }}>Sign In</button>
              <button className="btn btn-blue" onClick={() => window.location.href = '/signup'} style={{ width: 'auto', padding: '8px 12px' }}>Sign Up</button>
            </>
          ) : (
            <>
              <button className="btn" onClick={() => window.location.href = '/profile'} style={{ width: 'auto', padding: '8px 12px' }}>Profile</button>
              <button className="btn" onClick={logout} style={{ width: 'auto', padding: '8px 12px' }}>Sign Out</button>
            </>
          )}
        </div>
      </header>

      <main className="app-main single">
        <Routes>
          <Route path="/" element={<div style={{ maxWidth: 680, margin: '0 auto' }}>
            <div className="panel">
              <div className="panel-header">
                <div className="panel-icon blue">🔐</div>
                <div>
                  <div className="panel-title">Welcome</div>
                  <div className="panel-desc">Sign in or sign up to continue</div>
                </div>
              </div>
              <div style={{ display: 'flex', gap: 12 }}>
                <button className="btn btn-blue" onClick={() => window.location.href = '/login'}>Sign In</button>
                <button className="btn" onClick={() => window.location.href = '/signup'}>Sign Up</button>
              </div>
            </div>
          </div>} />
          <Route path="/request" element={<BloodRequest />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </main>
    </div>
  );
}

function App() {
  return (
    <BrowserRouter>
      <Layout />
    </BrowserRouter>
  );
}

export default App;
