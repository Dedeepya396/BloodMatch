import { useState } from "react";
import MapPicker from "./MapPicker";

function AddDonor() {
  const [donor, setDonor] = useState({
    name: "",
    bloodGroup: "",
    latitude: "",
    longitude: "",
    available: true,
    lastDonationDate: ""
  });

  const [toastVisible, setToastVisible] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setDonor({ ...donor, [name]: type === "checkbox" ? checked : value });
  };

  const handleSubmit = async () => {
    setLoading(true);
    try {
      await fetch("http://localhost:8080/api/donors", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(donor)
      });
      setToastVisible(true);
      setTimeout(() => setToastVisible(false), 3000);
      setDonor({ name: "", bloodGroup: "", latitude: "", longitude: "", available: true, lastDonationDate: "" });
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const bloodGroups = ["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"];


  const onMapChange = ({ lat, lng, display_name }) => {
    setDonor({ ...donor, latitude: String(lat), longitude: String(lng) });
    if (display_name) {
      // optional: store address if needed
      setDonor(prev => ({ ...prev, address: display_name }));
    }
  };

  return (
    <>
      <div className="panel">
        <div className="panel-header">
          <div className="panel-icon blue">💉</div>
          <div>
            <div className="panel-title">Register Donor</div>
            <div className="panel-desc">Add a new blood donor to the network</div>
          </div>
        </div>

        <div className="form-grid">
          <div className="field span-2">
            <label>Full Name</label>
            <input
              name="name"
              value={donor.name}
              placeholder="e.g. Arjun Mehta"
              onChange={handleChange}
            />
          </div>

          <div className="field">
            <label>Blood Group</label>
            <select name="bloodGroup" value={donor.bloodGroup} onChange={handleChange}>
              <option value="">Select group</option>
              {bloodGroups.map(g => (
                <option key={g} value={g}>{g}</option>
              ))}
            </select>
          </div>

          <div className="field">
            <label>Last Donation</label>https://nominatim.openstreetmap.org/search?format
            <input
              name="lastDonationDate"
              type="date"
              value={donor.lastDonationDate}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="divider" />

        <div className="coord-row" style={{gridTemplateColumns: '1fr'}}>
          <div className="field span-2">
            <label>Select location</label>
            <MapPicker
              position={donor.latitude && donor.longitude ? [parseFloat(donor.latitude), parseFloat(donor.longitude)] : null}
              onChange={onMapChange}
            />
          </div>
        </div>

        <button
          className="btn btn-blue"
          onClick={handleSubmit}
          disabled={loading}
        >
          {loading ? (
            <>⏳ Registering…</>
          ) : (
            <>+ Register Donor</>
          )}
        </button>
      </div>

      <div className={`toast${toastVisible ? " show" : ""}`}>
        ✓ Donor registered successfully
      </div>
    </>
  );
}

export default AddDonor;
