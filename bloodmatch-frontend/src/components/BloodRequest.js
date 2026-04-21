import { useState, useEffect } from "react";
import MapPicker from "./MapPicker";

function BloodRequest() {
  const [request, setRequest] = useState({
    hospitalName: "",
    bloodGroupRequired: "",
    unitsRequired: "",
    latitude: "",
    longitude: "",
    urgency: ""
  });

  const [matches, setMatches] = useState([]);
  const [hospitalRequests, setHospitalRequests] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);

  const handleChange = (e) =>
    setRequest({ ...request, [e.target.name]: e.target.value });

useEffect(() => {
    const auth = JSON.parse(localStorage.getItem("bm_auth"));
    const hospitalName = auth?.name;
  if (!hospitalName) {
    console.log("No hospital name found");
    return;
  }
  console.log("HOSPITAL NAME:",hospitalName);
  const fetchHospitalRequests = async () => {
    try {
      const r = await fetch(
        `http://localhost:8080/api/requests/hospital/${encodeURIComponent(hospitalName)}`
      );
      if (r.ok) {
        setHospitalRequests(await r.json());
      }
    } catch (e) {
      console.warn("Failed fetching hospital requests", e);
    }
  };

  fetchHospitalRequests();
}, []);

  const handleSubmit = async () => {
    setLoading(true);
    setSearched(false);
    try {
      const res = await fetch("http://localhost:8080/api/requests/match", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request)
      });

      const data = await res.json();
      setMatches(data);
    } catch (err) {
      console.error(err);
      setMatches([]);
    } finally {
      setLoading(false);
      setSearched(true);
    }
  };

  const bloodGroups = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];

  const onMapChange = ({ lat, lng, display_name }) => {
    setRequest((prev) => ({
      ...prev,
      latitude: String(lat),
      longitude: String(lng),
      address: display_name || prev.address
    }));
  };

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
      </div>

      <div className="divider" />

      <div className="field span-2">
        <label>Select location</label>
        <MapPicker
          position={
            request.latitude && request.longitude
              ? [
                  parseFloat(request.latitude),
                  parseFloat(request.longitude)
                ]
              : null
          }
          onChange={onMapChange}
        />
      </div>

      <div style={{ marginTop: 12 }}>
        <button
          className="btn btn-red"
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? "⏳ Searching donors…" : "🔍 Find Matching Donors"}
        </button>
      </div>

      {(searched || matches.length > 0) && (
        <>
          <div className="matches-header">
            Matched Donors
            <span className="match-count">{matches.length} found</span>
          </div>

          {matches.length === 0 ? (
            <div className="empty-state">
              No matching donors found nearby.
              <br />
              Try expanding the search radius or checking blood group.
            </div>
          ) : (
            <ul className="matches-list">
              {matches.map((m, index) => (
                <li
                  className="match-item"
                  key={index}
                  style={{ animationDelay: `${index * 0.05}s` }}
                >
                  {m.type === "BANK" ? (
                    <>
                      <div>
                        <div className="match-name">
                          {m.name}{" "}
                          <span className="bank-badge">Bank</span>
                        </div>
                        <div className="match-meta">
                          {m.distanceKm != null
                            ? `${m.distanceKm.toFixed(1)} km away`
                            : "Distance unknown"}
                          {m.availableUnits != null &&
                            ` · ${m.availableUnits} units available`}
                        </div>
                      </div>
                      <span className="blood-badge">{m.bloodGroup}</span>
                    </>
                  ) : (
                    <>
                      <div>
                        <div className="match-name">
                          {m.donor.name}
                        </div>
                        <div className="match-meta">
                          {m.distanceKm != null
                            ? `${m.distanceKm.toFixed(1)} km away`
                            : "Distance unknown"}
                          {m.donor.available !== undefined &&
                            ` · ${
                              m.donor.available
                                ? "Available"
                                : "Unavailable"
                            }`}
                          {m.exactMatch && (
                            <span className="exact-badge">
                              {" "}
                              · Exact Match ✓
                            </span>
                          )}
                        </div>
                      </div>
                      <span className="blood-badge">
                        {m.donor.bloodGroup}
                      </span>
                    </>
                  )}
                </li>
              ))}
            </ul>
          )}
        </>
      )}

      {/* Hospital Requests */}
      {true && (
        <div
          style={{
            marginTop: 18,
            padding: 12,
            border: "1px solid rgba(0,0,0,0.06)",
            borderRadius: 8
          }}
        >
          <h4>Your Requests</h4>

          {hospitalRequests.length === 0 ? (
            <div style={{ color: "#666" }}>
              No requests found for {request.hospitalName}.
            </div>
          ) : (
            <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
              {hospitalRequests.map((hr) => (
                <li
                  key={hr.requestId}
                  style={{
                    padding: 8,
                    borderBottom: "1px solid rgba(0,0,0,0.04)"
                  }}
                >
                  <div style={{ fontWeight: 700 }}>
                    {hr.bloodGroup} · Requested:{" "}
                    {hr.totalRequested} units
                  </div>
                  <div style={{ color: "#666" }}>
                    Allocated: {hr.allocated} · Remaining:{" "}
                    {hr.remaining}
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}

export default BloodRequest;