import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";

const API = "http://localhost:8080/api/bloodbank";

const BLOOD_GROUPS = ["All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];

const STATUS_COLORS = {
  AVAILABLE: { bg: "rgba(34,197,94,0.15)", color: "#4ade80", border: "#22c55e" },
  DONATED: { bg: "rgba(59,130,246,0.15)", color: "#60a5fa", border: "#3b82f6" },
  EXPIRED: { bg: "rgba(239,68,68,0.15)", color: "#f87171", border: "#ef4444" },
};

function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(iso);
  return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
}

function daysUntilExpiry(expiryDate) {
  const today = new Date(); today.setHours(0, 0, 0, 0);
  const exp = new Date(expiryDate); exp.setHours(0, 0, 0, 0);
  return Math.round((exp - today) / 86400000);
}

// ─── Stat Card ───────────────────────────────────────────────────────────────
function StatCard({ icon, label, value, color }) {
  return (
    <div style={{
      background: "rgba(255,255,255,0.04)", border: "1px solid rgba(255,255,255,0.08)",
      borderRadius: 14, padding: "18px 22px", flex: 1, minWidth: 140,
      display: "flex", flexDirection: "column", gap: 6
    }}>
      <div style={{ fontSize: 26 }}>{icon}</div>
      <div style={{ fontSize: 28, fontWeight: 700, color: color || "#1e293b" }}>{value}</div>
      <div style={{ fontSize: 12, color: "var(--text-secondary)", letterSpacing: "0.04em" }}>{label}</div>
    </div>
  );
}

// ─── Packet Row ──────────────────────────────────────────────────────────────
function PacketRow({ packet, index }) {
  const days = daysUntilExpiry(packet.expiryDate);
  const sc = STATUS_COLORS[packet.status] || STATUS_COLORS.AVAILABLE;

  return (
    <tr style={{ borderBottom: "1px solid rgba(255,255,255,0.05)", transition: "background 0.15s" }}
      className="packet-row">
      <td style={{ padding: "10px 14px", fontWeight: 700, color: "var(--text-muted)" }}>{index + 1}</td>
      <td style={{ padding: "10px 14px" }}>
        <span style={{ background: "rgba(239,68,68,0.18)", color: "#f87171", borderRadius: 6, padding: "3px 10px", fontWeight: 700, fontSize: 13 }}>
          {packet.bloodGroup}
        </span>
      </td>
      <td style={{ padding: "10px 14px", fontWeight: 600, color: "var(--text-primary)", textAlign: "center" }}>{packet.units}</td>
      <td style={{ padding: "10px 14px", color: "var(--text-secondary)", fontSize: 13 }}>{formatDate(packet.collectedDate)}</td>
      <td style={{ padding: "10px 14px", fontSize: 13 }}>
        <span style={{ color: days < 0 ? "#f87171" : days <= 7 ? "#fbbf24" : "var(--text-secondary)" }}>
          {formatDate(packet.expiryDate)}
          {packet.status === "AVAILABLE" && (
            <span style={{ marginLeft: 6, fontSize: 11, opacity: 0.8 }}>
              {days < 0 ? "(expired)" : `(${days}d left)`}
            </span>
          )}
        </span>
      </td>
      <td style={{ padding: "10px 14px" }}>
        <span style={{ background: sc.bg, color: sc.color, border: `1px solid ${sc.border}`, borderRadius: 20, padding: "3px 12px", fontSize: 12, fontWeight: 600 }}>
          {packet.status}
        </span>
      </td>
    </tr>
  );
}

// ─── Main Dashboard ───────────────────────────────────────────────────────────
function BloodBankDashboard() {
  const navigate = useNavigate();
  const [auth, setAuth] = useState(null);
  const [packets, setPackets] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState("All");
  const [showExpired, setShowExpired] = useState(false);
  const [expiredPackets, setExpiredPackets] = useState([]);
  const [loading, setLoading] = useState(false);
  const [addLoading, setAddLoading] = useState(false);
  const [form, setForm] = useState({ bloodGroup: "A+", collectedDate: "", units: "" });
  const [addError, setAddError] = useState("");
  const [addSuccess, setAddSuccess] = useState("");
  const [showAddForm, setShowAddForm] = useState(false);
  const [toast, setToast] = useState("");
  const [showDonated, setShowDonated] = useState(false);
  const [donatedPackets, setDonatedPackets] = useState([]);
  const [bankRequests, setBankRequests] = useState([]);
  const [allocateForm, setAllocateForm] = useState({ bloodGroup: "A+", units: "" });
  const [allocateLoading, setAllocateLoading] = useState(false);

  useEffect(() => {
    const raw = localStorage.getItem("bm_auth");
    if (!raw) { navigate("/login"); return; }
    const a = JSON.parse(raw);
    if (a.role !== "BLOOD_BANK") { navigate("/"); return; }
    setAuth(a);
  }, [navigate]);

  const fetchPackets = useCallback(async (bankId) => {
    setLoading(true);
    try {
      const res = await fetch(`${API}/packets/${bankId}`);
      if (res.ok) setPackets(await res.json());
    } finally { setLoading(false); }
  }, []);

  const fetchBankRequests = useCallback(async (bankId) => {
    try {
      const res = await fetch(`http://localhost:8080/api/bank-requests/bank/${bankId}`);
      if (res.ok) setBankRequests(await res.json());
    } catch (e) { }
  }, []);

  useEffect(() => {
    if (auth) fetchPackets(auth.id);
    if (auth) fetchBankRequests(auth.id);
  }, [auth, fetchPackets, fetchBankRequests]);

  const fetchExpired = async () => {
    const res = await fetch(`${API}/packets/${auth.id}/expired`);
    if (res.ok) setExpiredPackets(await res.json());
    setShowExpired(true);
  };

  const fetchDonated = async () => {
    const res = await fetch(`${API}/packets/${auth.id}/donated`);
    if (res.ok) setDonatedPackets(await res.json());
    setShowDonated(true);
  };

  const handleAllocate = async () => {
    if (!allocateForm.units || parseInt(allocateForm.units) <= 0) {
      showToast("Units must be greater than 0");
      return;
    }
    setAllocateLoading(true);
    try {
      const res = await fetch(`${API}/packets/${auth.id}/allocate`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ bloodGroup: allocateForm.bloodGroup, units: parseInt(allocateForm.units) })
      });
      const data = await res.json();
      if (res.ok) {
        if (data.shortfall > 0) {
          showToast(`Allocated ${data.allocated} units. ${data.shortfall} unit(s) less than required! ⚠️`);
        } else {
          showToast(`Successfully allocated ${data.allocated} units of ${allocateForm.bloodGroup}`);
        }
        setAllocateForm(f => ({ ...f, units: "" }));
        fetchPackets(auth.id);
        fetchBankRequests(auth.id);
        if (showDonated) fetchDonated();
      } else {
        const err = await res.text();
        showToast("Allocation failed: " + err);
      }
    } catch {
      showToast("Network error during allocation");
    } finally {
      setAllocateLoading(false);
    }
  };

  const handleBankAction = async (id, action) => {
    try {
      const res = await fetch(`http://localhost:8080/api/bank-requests/${id}/${action}`, { method: 'PATCH' });
      if (res.ok) {
        showToast(action === 'accept' ? 'Request accepted' : 'Request rejected');
        fetchBankRequests(auth.id);
        fetchPackets(auth.id);
      } else {
        const t = await res.text(); showToast('Action failed: ' + t);
      }
    } catch (e) { showToast('Network error'); }
  };

  const handleAdd = async () => {
    setAddError(""); setAddSuccess("");
    if (!form.collectedDate) { setAddError("Please select a collected date."); return; }
    if (!form.units || parseInt(form.units) <= 0) { setAddError("Units must be a positive number."); return; }
    setAddLoading(true);
    try {
      const res = await fetch(`${API}/packets/${auth.id}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ bloodGroup: form.bloodGroup, collectedDate: form.collectedDate, units: parseInt(form.units) })
      });
      if (!res.ok) { const t = await res.text(); setAddError(t || "Failed to add packet"); return; }
      setAddSuccess("Packet added successfully!");
      setForm(f => ({ ...f, collectedDate: "", units: "" }));
      setTimeout(() => setAddSuccess(""), 3000);
      fetchPackets(auth.id);
      setShowAddForm(false);
    } catch { setAddError("Network error"); }
    finally { setAddLoading(false); }
  };

  const handleAction = async (id, action) => {
    let url = action === "delete" ? `${API}/packets/${id}` : `${API}/packets/${id}/${action}`;
    let method = action === "delete" ? "DELETE" : "PATCH";
    const res = await fetch(url, { method });
    if (res.ok) {
      showToast(action === "donate" ? "Marked as Donated " : action === "expire" ? "Marked as Expired " : "Packet Removed 🗑");
      fetchPackets(auth.id);
      if (showExpired) fetchExpired();
    }
  };

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(""), 2500);
  };

  const displayed = packets.filter(p => selectedGroup === "All" || p.bloodGroup === selectedGroup);

  // Stats
  const availablePackets = packets.filter(p => p.status === "AVAILABLE");
  const totalUnits = availablePackets.reduce((s, p) => s + p.units, 0);
  const expiringSoon = availablePackets.filter(p => {
    const d = daysUntilExpiry(p.expiryDate);
    return d >= 0 && d <= 7;
  });
  const expiredCount = packets.filter(p => p.status === "EXPIRED").length;

  // Group summary for selected group
  const groupedUnits = selectedGroup !== "All"
    ? displayed.filter(p => p.status === "AVAILABLE").reduce((s, p) => s + p.units, 0)
    : totalUnits;

  if (!auth) return null;

  return (
    <div style={{ maxWidth: 1100, margin: "0 auto", padding: "0 0 40px" }}>

      {/* ── Toast ── */}
      {toast && (
        <div style={{
          position: "fixed", top: 20, right: 20, background: "rgba(30,41,59,0.98)",
          border: "1px solid rgba(255,255,255,0.12)", borderRadius: 10, padding: "12px 20px",
          color: "#e2e8f0", fontWeight: 600, zIndex: 9999, boxShadow: "0 8px 32px rgba(0,0,0,0.5)",
          animation: "slideIn 0.3s ease"
        }}>
          {toast}
        </div>
      )}

      {/* ── Header ── */}
      <div style={{ marginBottom: 28 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 14, marginBottom: 4 }}>
          <div style={{ width: 46, height: 46, borderRadius: 12, background: "linear-gradient(135deg,#ef4444,#b91c1c)", display: "flex", alignItems: "center", justifyContent: "center", fontSize: 22 }}>🏦</div>
          <div>
            <h2 style={{ margin: 0, fontSize: 22, fontWeight: 800, color: "var(--text-primary)" }}>Blood Bank Dashboard</h2>
            <p style={{ margin: 0, color: "var(--text-secondary)", fontSize: 13 }}>Welcome, {auth.name}</p>
          </div>
        </div>
      </div>

      {/* ── Stats Row ── */}
      <div style={{ display: "flex", gap: 14, marginBottom: 28, flexWrap: "wrap" }}>
        <StatCard label="Total Packets" value={packets.length} color="#60a5fa" />
        <StatCard label="Available Units" value={totalUnits} color="#4ade80" />
        <StatCard label="Expiring in 7 days" value={expiringSoon.length} color="#fbbf24" />
        <StatCard label="Expired Packets" value={expiredCount} color="#f87171" />
      </div>

      {/* ── Action Buttons Row ── */}
      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(280px, 1fr))", gap: 16, marginBottom: 32 }}>
        {/* ADD PACKET TOGGLE */}
        <div
          onClick={() => setShowAddForm(!showAddForm)}
          style={{
            background: showAddForm ? "rgba(220,38,38,0.05)" : "white",
            border: showAddForm ? "1px solid #dc2626" : "1px solid var(--border)",
            borderRadius: 16, padding: "20px 24px", cursor: "pointer", transition: "all 0.2s",
            display: "flex", alignItems: "center", justifyContent: "space-between",
            boxShadow: "0 2px 4px rgba(0,0,0,0.02)"
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <div className="panel-icon" style={{ background: "#fff1f2", color: "#dc2626" }}>➕</div>
            <div>
              <div className="panel-title" style={{ margin: 0, fontSize: '15px' }}>Add Blood Packet</div>
              <div className="panel-desc" style={{ margin: 0 }}>Register new inventory</div>
            </div>
          </div>
        </div>

        {/* SHOW DONATED TOGGLE */}
        <div
          onClick={() => { if (!showDonated) fetchDonated(); else setShowDonated(false); }}
          style={{
            background: showDonated ? "rgba(5,150,105,0.05)" : "white",
            border: showDonated ? "1px solid #059669" : "1px solid var(--border)",
            borderRadius: 16, padding: "20px 24px", cursor: "pointer", transition: "all 0.2s",
            display: "flex", alignItems: "center", justifyContent: "space-between",
            boxShadow: "0 2px 4px rgba(0,0,0,0.02)"
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <div className="panel-icon" style={{ background: "#ecfdf5", color: "#059669" }}>🩸</div>
            <div>
              <div className="panel-title" style={{ margin: 0, fontSize: '15px' }}>Donated Packets</div>
              <div className="panel-desc" style={{ margin: 0 }}>View dispatched records</div>
            </div>
          </div>
        </div>

        {/* SHOW EXPIRED TOGGLE */}
        <div
          onClick={() => { if (!showExpired) fetchExpired(); else setShowExpired(false); }}
          style={{
            background: showExpired ? "rgba(220,38,38,0.05)" : "white",
            border: showExpired ? "1px solid #dc2626" : "1px solid var(--border)",
            borderRadius: 16, padding: "20px 24px", cursor: "pointer", transition: "all 0.2s",
            display: "flex", alignItems: "center", justifyContent: "space-between",
            boxShadow: "0 2px 4px rgba(0,0,0,0.02)"
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: 16 }}>
            <div className="panel-icon" style={{ background: "#fff1f2", color: "#dc2626" }}>⚠️</div>
            <div>
              <div className="panel-title" style={{ margin: 0, fontSize: '15px' }}>Expired Packets</div>
              <div className="panel-desc" style={{ margin: 0 }}>Manage expiring units</div>
            </div>
          </div>
          <button style={{
            background: showExpired ? "#dc2626" : "#f1f5f9",
            color: showExpired ? "white" : "#64748b",
            border: "none", borderRadius: 8, padding: "6px 12px", fontWeight: 700, fontSize: '11px', pointerEvents: "none"
          }}>
            {showExpired ? "Hide" : "Show"} ({expiredCount})
          </button>
        </div>
      </div>

      {/* Incoming Bank Requests (visible) */}
      <div className="panel" style={{ marginBottom: 18 }}>
        <div className="panel-header">
          <div className="panel-icon" style={{ background: "rgba(255,255,255,0.04)", color: "#fff" }}>📨</div>
          <div>
            <div className="panel-title">Incoming Hospital Requests</div>
            <div className="panel-desc">Requests assigned to your bank — accept to allocate</div>
          </div>
        </div>

        <div style={{ padding: 12 }}>
          {bankRequests.length === 0 ? (
            <div style={{ color: 'var(--text-secondary)' }}>No incoming requests.</div>
          ) : (
            <ul style={{ listStyle: 'none', padding: 0, margin: 0 }}>
              {bankRequests.map(r => (
                <li key={r.id} style={{ background: 'rgba(255,255,255,0.02)', border: '1px solid rgba(255,255,255,0.06)', padding: 12, borderRadius: 10, marginBottom: 8, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ fontWeight: 700 }}>{r.hospitalName} · {r.bloodGroup} · {r.unitsRequested} units</div>
                    <div style={{ color: 'var(--text-secondary)', fontSize: 13 }}>Requested at: {new Date(r.createdAt).toLocaleString()}</div>
                  </div>
                  <div style={{ display: 'flex', gap: 8 }}>
                    {r.status === 'PENDING' ? (
                      <>
                        <button onClick={() => handleBankAction(r.id, 'accept')} style={{ background: '#22c55e', color: '#fff', border: 'none', padding: '8px 12px', borderRadius: 8 }}>Accept</button>
                        <button onClick={() => handleBankAction(r.id, 'reject')} style={{ background: 'transparent', color: '#f87171', border: '1px solid rgba(248,113,113,0.12)', padding: '8px 12px', borderRadius: 8 }}>Reject</button>
                      </>
                    ) : (
                      <div style={{ color: '#94a3b8', fontWeight: 700 }}>{r.status}</div>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* ── Add Packet Form Panel ── */}
      {showAddForm && (
        <div className="panel" style={{ marginBottom: 24, animation: "slideIn 0.2s ease" }}>
          <div style={{ display: "flex", gap: 14, flexWrap: "wrap", alignItems: "flex-end" }}>
            <div className="field" style={{ flex: "0 0 160px" }}>
              <label>Blood Group</label>
              <select value={form.bloodGroup} onChange={e => setForm(f => ({ ...f, bloodGroup: e.target.value }))}
                style={{ background: "rgba(255,255,255,0.05)", border: "1px solid rgba(255,255,255,0.12)", borderRadius: 8, padding: "9px 12px", color: "#e2e8f0", width: "100%", fontSize: 14 }}>
                {BLOOD_GROUPS.filter(g => g !== "All").map(g => (
                  <option key={g} value={g} style={{ backgroundColor: "#0f172a", color: "#ffffff" }}>
                    {g}
                  </option>
                ))}
              </select>
            </div>
            <div className="field" style={{ flex: "0 0 200px" }}>
              <label>Collected Date</label>
              <input type="date" value={form.collectedDate}
                onChange={e => setForm(f => ({ ...f, collectedDate: e.target.value }))}
                max={new Date().toISOString().split("T")[0]}
              />
            </div>
            <div className="field" style={{ flex: "0 0 130px" }}>
              <label>Units</label>
              <input type="number" min="1" placeholder="e.g. 2" value={form.units}
                onChange={e => setForm(f => ({ ...f, units: e.target.value }))} />
            </div>
            <div className="field" style={{ flex: "0 0 auto", paddingBottom: 0 }}>
              <button className="btn btn-blue" onClick={handleAdd} disabled={addLoading}
                style={{ marginTop: 0, padding: "10px 22px", fontWeight: 700 }}>
                {addLoading ? "Adding…" : "Add Packet"}
              </button>
            </div>
          </div>

          {addError && <div style={{ color: "#f87171", marginTop: 10, fontSize: 13 }}>⚠ {addError}</div>}
          {addSuccess && <div style={{ color: "#4ade80", marginTop: 10, fontSize: 13 }}>✔ {addSuccess}</div>}

          {form.collectedDate && (
            <div style={{ marginTop: 10, fontSize: 12, color: "#7a9bbf" }}>
              Expiry will be: <strong style={{ color: "#fbbf24" }}>
                {formatDate(new Date(new Date(form.collectedDate).getTime() + 42 * 86400000).toISOString())}
              </strong> &nbsp;(collected date + 42 days)
            </div>
          )}
        </div>
      )}

      {/* ── Donated Panel & Request Tester ── */}
      {showDonated && (
        <div style={{ background: "rgba(34,197,94,0.05)", border: "1px solid rgba(34,197,94,0.2)", borderRadius: 14, padding: "20px", marginBottom: 24, animation: "slideIn 0.2s ease" }}>

          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-end", borderBottom: "1px solid rgba(34,197,94,0.2)", paddingBottom: 16, marginBottom: 16 }}>
            <div>
              <h3 style={{ margin: "0 0 8px", color: "#4ade80", fontSize: 16, fontWeight: 700 }}>Donated Records & Requests</h3>
              <p style={{ margin: 0, fontSize: 13, color: "#94a3b8" }}>Allocated blood details or test hospital request distribution here.</p>
            </div>

            {/* Simulation Block */}
            <div style={{ display: "flex", alignItems: "flex-end", gap: 10, background: "rgba(255,255,255,0.04)", padding: "12px", borderRadius: 10 }}>
              <div>
                <label style={{ display: "block", fontSize: 12, marginBottom: 4, color: "#7a9bbf" }}>Blood Group</label>
                <select value={allocateForm.bloodGroup} onChange={e => setAllocateForm(f => ({ ...f, bloodGroup: e.target.value }))}
                  style={{ background: "#fff", border: "1px solid var(--border)", borderRadius: 6, padding: "6px 10px", color: "var(--text-primary)" }}>
                  {BLOOD_GROUPS.filter(g => g !== "All").map(g => <option key={g} value={g}>{g}</option>)}
                </select>
              </div>
              <div>
                <label style={{ display: "block", fontSize: 12, marginBottom: 4, color: "var(--text-secondary)" }}>Req. Units</label>
                <input type="number" min="1" value={allocateForm.units} onChange={e => setAllocateForm(f => ({ ...f, units: e.target.value }))}
                  style={{ width: 60, background: "#fff", border: "1px solid var(--border)", borderRadius: 6, padding: "6px 10px", color: "var(--text-primary)", WebkitAppearance: "none", margin: 0 }} />
              </div>
              <button
                onClick={handleAllocate} disabled={allocateLoading}
                style={{ background: "#22c55e", color: "white", border: "none", borderRadius: 6, padding: "8px 14px", fontWeight: "bold", cursor: "pointer", fontSize: 13 }}>
                {allocateLoading ? "Allocating..." : "Test Fulfill Request"}
              </button>
            </div>
          </div>

          {donatedPackets.length === 0 ? (
            <p style={{ color: "#7a9bbf", fontSize: 14 }}>No donated blood records assigned yet.</p>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr style={{ borderBottom: "1px solid rgba(34,197,94,0.3)" }}>
                    {["Original Packet ID", "Blood Group", "Units Donated", "Allocated Date"].map(h => (
                      <th key={h} style={{ padding: "8px 14px", textAlign: "left", fontSize: 11, color: "#4ade80", textTransform: "uppercase", letterSpacing: "0.06em", fontWeight: 700 }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {donatedPackets.map(r => (
                    <tr key={r.id} style={{ borderBottom: "1px solid rgba(34,197,94,0.1)" }}>
                      <td style={{ padding: "9px 14px", fontFamily: "monospace", fontSize: 11, color: "#94a3b8" }}>{r.originalPacketId}</td>
                      <td style={{ padding: "9px 14px" }}>
                        <span style={{ background: "rgba(34,197,94,0.18)", color: "#4ade80", borderRadius: 6, padding: "2px 9px", fontWeight: 700 }}>{r.bloodGroup}</span>
                      </td>
                      <td style={{ padding: "9px 14px", color: "#e2e8f0", fontWeight: 600 }}>{r.unitsAllocated} units</td>
                      <td style={{ padding: "9px 14px", color: "#94a3b8", fontSize: 13 }}>{formatDate(r.allocationDate)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Incoming Bank Requests (moved to top) */}
        </div>
      )}

      {/* ── Expired Panel ── */}
      {showExpired && (
        <div style={{ background: "rgba(239,68,68,0.06)", border: "1px solid rgba(239,68,68,0.25)", borderRadius: 14, padding: "20px", marginBottom: 24 }}>
          <h3 style={{ margin: "0 0 16px", color: "#f87171", fontSize: 16, fontWeight: 700 }}> Expired Packets — Quick Removal</h3>
          {expiredPackets.length === 0 ? (
            <p style={{ color: "#7a9bbf", fontSize: 14 }}>No expired packets found.</p>
          ) : (
            <div style={{ overflowX: "auto" }}>
              <table style={{ width: "100%", borderCollapse: "collapse" }}>
                <thead>
                  <tr style={{ borderBottom: "1px solid rgba(239,68,68,0.3)" }}>
                    {["Packet ID", "Blood Group", "Units", "Collected", "Expired On", "Action"].map(h => (
                      <th key={h} style={{ padding: "8px 14px", textAlign: "left", fontSize: 11, color: "#f87171", textTransform: "uppercase", letterSpacing: "0.06em", fontWeight: 700 }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {expiredPackets.map(p => (
                    <tr key={p.id} style={{ borderBottom: "1px solid rgba(239,68,68,0.1)" }}>
                      <td style={{ padding: "9px 14px", fontFamily: "monospace", fontSize: 11, color: "#94a3b8", maxWidth: 110, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={p.id}>{p.id}</td>
                      <td style={{ padding: "9px 14px" }}>
                        <span style={{ background: "rgba(239,68,68,0.18)", color: "#f87171", borderRadius: 6, padding: "2px 9px", fontWeight: 700 }}>{p.bloodGroup}</span>
                      </td>
                      <td style={{ padding: "9px 14px", color: "#e2e8f0", fontWeight: 600 }}>{p.units}</td>
                      <td style={{ padding: "9px 14px", color: "#94a3b8", fontSize: 13 }}>{formatDate(p.collectedDate)}</td>
                      <td style={{ padding: "9px 14px", color: "#f87171", fontSize: 13 }}>{formatDate(p.expiryDate)}</td>
                      <td style={{ padding: "9px 14px" }}>
                        <button onClick={() => { if (window.confirm("Are you sure you want to permanently remove this packet from the database?")) handleAction(p.id, "delete") }}
                          style={{ background: "rgba(239,68,68,0.2)", color: "#f87171", border: "1px solid #ef4444", borderRadius: 6, padding: "5px 14px", cursor: "pointer", fontWeight: 600, fontSize: 12 }}>
                          Remove
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ── Inventory Browser ── */}
      <div className="panel">
        <div className="panel-header">
          <div className="panel-icon" style={{ background: "rgba(59,130,246,0.18)", color: "#60a5fa" }}>📦</div>
          <div>
            <div className="panel-title">Inventory Browser</div>
            <div className="panel-desc">Browse and manage all blood packets</div>
          </div>
        </div>

        {/* Blood Group Filter Tabs */}
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 20 }}>
          {BLOOD_GROUPS.map(g => {
            const count = g === "All" ? packets.length : packets.filter(p => p.bloodGroup === g).length;
            return (
              <button key={g} onClick={() => setSelectedGroup(g)}
                style={{
                  background: selectedGroup === g ? "rgba(239,68,68,0.25)" : "rgba(255,255,255,0.05)",
                  color: selectedGroup === g ? "#f87171" : "#94a3b8",
                  border: selectedGroup === g ? "1px solid #ef4444" : "1px solid rgba(255,255,255,0.1)",
                  borderRadius: 20, padding: "6px 16px", cursor: "pointer", fontWeight: selectedGroup === g ? 700 : 500,
                  fontSize: 13, transition: "all 0.15s", display: "flex", alignItems: "center", gap: 6
                }}>
                {g}
                {count > 0 && <span style={{ background: "rgba(255,255,255,0.1)", borderRadius: 20, padding: "1px 7px", fontSize: 11 }}>{count}</span>}
              </button>
            );
          })}
        </div>

        {/* Summary bar */}
        {selectedGroup !== "All" && (
          <div style={{
            background: "rgba(34,197,94,0.05)", border: "1px solid rgba(34,197,94,0.1)",
            borderRadius: 10, padding: "12px 18px", marginBottom: 16,
            display: "flex", alignItems: "center", gap: 10
          }}>
            <span style={{ fontSize: 20 }}></span>
            <div>
              <span style={{ color: "var(--text-secondary)", fontSize: 13 }}>Total usable units for </span>
              <span style={{ color: "var(--accent-red)", fontWeight: 700 }}>{selectedGroup}</span>
              <span style={{ color: "var(--text-secondary)", fontSize: 13 }}> → </span>
              <span style={{ color: "#059669", fontWeight: 800, fontSize: 18 }}>{groupedUnits}</span>
              <span style={{ color: "var(--text-secondary)", fontSize: 13 }}> units (AVAILABLE, not expired)</span>
            </div>
          </div>
        )}

        {loading ? (
          <div style={{ textAlign: "center", color: "#7a9bbf", padding: 40 }}>Loading packets…</div>
        ) : displayed.length === 0 ? (
          <div style={{ textAlign: "center", color: "#7a9bbf", padding: 40 }}>
            <div style={{ fontSize: 40, marginBottom: 12 }}>🩸</div>
            <p>No packets found{selectedGroup !== "All" ? ` for ${selectedGroup}` : ""}.</p>
          </div>
        ) : (
          <div style={{ overflowX: "auto" }}>
            <table style={{ width: "100%", borderCollapse: "collapse" }}>
              <thead>
                <tr style={{ borderBottom: "1px solid rgba(255,255,255,0.1)" }}>
                  {["#", "Blood Group", "Units", "Collected Date", "Expiry Date", "Status"].map(h => (
                    <th key={h} style={{ padding: "10px 14px", textAlign: "left", fontSize: 11, color: "#7a9bbf", textTransform: "uppercase", letterSpacing: "0.06em", fontWeight: 700 }}>{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {displayed.map((p, index) => (
                  <PacketRow key={p.id} packet={p} index={index} />
                ))}
              </tbody>
            </table>

            {/* Footer total */}
            <div style={{
              borderTop: "1px solid rgba(255,255,255,0.08)", marginTop: 12,
              padding: "14px 14px 0", display: "flex", justifyContent: "flex-end", gap: 20
            }}>
              <span style={{ color: "var(--text-secondary)", fontSize: 13 }}>
                Showing <strong style={{ color: "var(--text-primary)" }}>{displayed.length}</strong> packet(s)
              </span>
              <span style={{ color: "var(--text-secondary)", fontSize: 13 }}>
                Total usable units: <strong style={{ color: "#059669", fontSize: 16 }}>{groupedUnits}</strong>
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Inline style for row hover */}
      <style>{`
        .packet-row:hover { background: rgba(255,255,255,0.03) !important; }
        @keyframes slideIn { from { opacity:0; transform: translateX(30px); } to { opacity:1; transform: translateX(0); } }
      `}</style>
    </div>
  );
}

export default BloodBankDashboard;
