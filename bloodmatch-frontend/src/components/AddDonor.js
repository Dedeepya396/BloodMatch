import { useState } from "react";

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
            <label>Last Donation</label>
            <input
              name="lastDonationDate"
              type="date"
              value={donor.lastDonationDate}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="divider" />

        <div className="coord-row">
          <div className="field">
            <label>Latitude</label>
            <input
              name="latitude"
              value={donor.latitude}
              placeholder="e.g. 17.3850"
              onChange={handleChange}
            />
          </div>
          <div className="field">
            <label>Longitude</label>
            <input
              name="longitude"
              value={donor.longitude}
              placeholder="e.g. 78.4867"
              onChange={handleChange}
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
