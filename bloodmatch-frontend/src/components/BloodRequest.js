import { useState, useEffect } from "react";

function BloodRequest() {
  const [request, setRequest] = useState({
    hospitalName: "",
    bloodGroupRequired: "",
    unitsRequired: "",
    latitude: "",
    longitude: "",
    urgency: "",
    hospitalId: ""
  });

  const [matches, setMatches] = useState([]);
  const [hospitalRequests, setHospitalRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const pageSize = 20;

  const handleChange = (e) =>
    setRequest({ ...request, [e.target.name]: e.target.value });

  useEffect(() => {
    const auth = JSON.parse(localStorage.getItem("bm_auth"));
    if (!auth || auth.role !== 'HOSPITAL') return;

    const fetchHospitalData = async () => {
      try {
        // Fetch full hospital details to get location
        const hospitalRes = await fetch(`http://localhost:8080/api/hospitals/by-email?email=${encodeURIComponent(auth.email)}`);
        if (hospitalRes.ok) {
          const data = await hospitalRes.json();
          setRequest(prev => ({
            ...prev,
            hospitalName: data.name || auth.name,
            latitude: String(data.latitude || ""),
            longitude: String(data.longitude || ""),
            address: data.address || "",
            hospitalId: data.id || ""
          }));
        }

        // Fetch existing requests
        const requestsRes = await fetch(
          `http://localhost:8080/api/requests/hospital/${encodeURIComponent(auth.name)}`
        );
        if (requestsRes.ok) {
          setHospitalRequests(await requestsRes.json());
        }
      } catch (e) {
        console.warn("Failed fetching hospital data", e);
      }
    };

    fetchHospitalData();
  }, []);

  const handleSubmit = async () => {
    setLoading(true);
    setSearched(false);
    try {
      // Ensure numeric fields are sent as numbers, not strings
      const payload = {
        ...request,
        unitsRequired: Number(request.unitsRequired) || 0,
        latitude: parseFloat(request.latitude) || 0,
        longitude: parseFloat(request.longitude) || 0,
      };

      const res = await fetch("http://localhost:8080/api/requests/match", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      const data = await res.json();
      // Guard: backend might return an error object instead of an array
      setMatches(Array.isArray(data) ? data : []);
      if (!Array.isArray(data)) {
        console.error("Unexpected response from /api/requests/match:", data);
      }
    } catch (err) {
      console.error(err);
      setMatches([]);
    } finally {
      setLoading(false);
      setSearched(true);
    }
  };

  const bloodGroups = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];


  return (
    <div className="panel">
      <div className="panel-header">
        <div className="panel-icon red">🩸</div>
        <div>
          <div className="panel-title">Blood Request</div>
          <div className="panel-desc">
            Find matched donors near your hospital
          </div>
        </div>
      </div>

      <div className="form-grid">
        <div className="field span-2">
          <label>Hospital Name</label>
          <input
            name="hospitalName"
            value={request.hospitalName}
            onChange={handleChange}
            readOnly
            style={{ background: '#f1f5f9', cursor: 'not-allowed' }}
          />
        </div>

        <div className="field">
          <label>Blood Group</label>
          <select
            name="bloodGroupRequired"
            value={request.bloodGroupRequired}
            onChange={handleChange}
          >
            <option value="">Select group</option>
            {bloodGroups.map((g) => (
              <option key={g} value={g}>
                {g}
              </option>
            ))}
          </select>
        </div>

        <div className="field">
          <label>Units Required</label>
          <input
            name="unitsRequired"
            value={request.unitsRequired}
            onChange={handleChange}
          />
        </div>

        <div className="field">
          <label>Urgency</label>
          <select
            name="urgency"
            value={request.urgency}
            onChange={handleChange}
          >
            <option value="">Select</option>
            <option value="HIGH">🔴 High</option>
            <option value="LOW">🟡 Low</option>
          </select>
        </div>
        <div className="divider" />

        <div className="field span-2">
          <label>Hospital Location (Auto-detected)</label>
          <input
            name="address"
            value={request.address || ""}
            readOnly
            style={{ background: '#f1f5f9', cursor: 'not-allowed' }}
          />
        </div>
      </div>

      <div style={{ marginTop: 12 }}>
        <button
          className="btn btn-red"
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? "Searching sources…" : "Search for Blood Sources"}
        </button>
      </div>

      {(searched || matches.length > 0) && (
        <>
          <div className="matches-header">
            Matched Results
            <span className="match-count">{matches.length} found</span>
          </div>

          {(request.urgency === "HIGH" || request.urgency === "LOW") && matches.length > 0 && matches[0].type === "DONOR" && (
            <div className="emergency-alert">
              <div>
                No blood banks has compatible blood. Emergency notification sent to all eligible donors who are in 20km radius.
              </div>
            </div>
          )}

          {matches.length > 0 && matches[0].type === "BANK" && (
            <div className="success-alert" style={{
              background: '#ecfdf5',
              border: '1px solid #6ee7b7',
              color: '#065f46',
              padding: '16px',
              borderRadius: '12px',
              marginBottom: '20px',
              fontWeight: 600,
              fontSize: '14px'
            }}>
              We requested blood to all these sources.
            </div>
          )}

          {matches.length === 0 ? (
            <div className="empty-state">
              No matching results found nearby.
              <br />
              Try expanding the search radius or checking blood group.
            </div>
          ) : (
            <>
              <div className="matches-grid">
                {matches.slice((currentPage - 1) * pageSize, currentPage * pageSize).map((m, index) => (
                  <div
                    className="match-card"
                    key={index}
                    style={{ animationDelay: `${index * 0.05}s` }}
                  >
                    <div className="match-card-header">
                      <div className="match-card-name">
                        {m.type === "BANK" ? "🏦 " : "👤 "}
                        {m.name}
                        {m.type === "BANK" && <span className="bank-badge">Bank</span>}
                      </div>
                      <span className="blood-badge">{m.bloodGroup}</span>
                    </div>

                    <div className="match-card-meta">
                      <div className="match-card-info">
                        📍 {m.distanceKm != null
                          ? `${m.distanceKm.toFixed(1)} km away`
                          : "Distance unknown"}
                      </div>
                      {m.type === "BANK" ? (
                        <div className="match-card-info">
                          📦 {m.availableUnits != null ? `${m.availableUnits} units available` : "Units unknown"}
                        </div>
                      ) : (
                        <div className="match-card-info">
                          🕒 {m.donor?.available ? "Available Now" : "Unavailable"}
                          {m.exactMatch && <span className="exact-badge" style={{ marginLeft: 8 }}>Exact Match</span>}
                        </div>
                      )}
                      {m.contactNumber && (
                        <div className="match-card-info">
                          📞 {m.contactNumber}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {matches.length > pageSize && (
                <div className="pagination">
                  {[...Array(Math.ceil(matches.length / pageSize))].map((_, i) => (
                    <button
                      key={i + 1}
                      className={`page-num ${currentPage === i + 1 ? 'active' : ''}`}
                      onClick={() => setCurrentPage(i + 1)}
                    >
                      {i + 1}
                    </button>
                  ))}
                </div>
              )}
            </>

          )}
        </>
      )}

      {/* Quick Links for Hospital */}
      <div className="divider" />
      <div style={{ display: 'flex', gap: 12, marginTop: 12 }}>
        <button className="btn btn-outline" onClick={() => window.location.href = '/pending-requests'}>
          View Pending Requests
        </button>
        <button className="btn btn-outline" onClick={() => window.location.href = '/accepted-requests'}>
          View Accepted Requests
        </button>
      </div>
    </div>
  );
}

export default BloodRequest;