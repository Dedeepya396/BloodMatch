import { useEffect, useState } from "react";
import MapPicker from "./MapPicker";

function Profile() {
  const [auth, setAuth] = useState(null);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [bloodBanks, setBloodBanks] = useState([]);
  const [showDonateForm, setShowDonateForm] = useState(false);
  const [donationForm, setDonationForm] = useState({ bloodBankId: '', units: 1, donationDate: new Date().toISOString().split('T')[0] });

  const location = window.location;
  useEffect(() => {
    const raw = localStorage.getItem('bm_auth');
    if (!raw) return;
    const parsed = JSON.parse(raw);
    setAuth(parsed);
    fetchProfile(parsed);
    if (parsed.role === 'DONOR') {
      fetchBloodBanks();
      if (location.search.includes('donate=true')) {
        setShowDonateForm(true);
      }
    }
  }, [location.search]);

  const fetchBloodBanks = async () => {
    try {
      const res = await fetch('http://localhost:8080/api/bloodbank');
      if (res.ok) setBloodBanks(await res.json());
    } catch (err) { console.error(err); }
  };

  const fetchProfile = async (parsed) => {
    setLoading(true);
    try {
      if (parsed.role === 'DONOR') {
        const res = await fetch(`http://localhost:8080/api/donors/by-email?email=${encodeURIComponent(parsed.email)}`);
        const obj = await res.json(); setData(obj);
      } else if (parsed.role === 'HOSPITAL') {
        const res = await fetch(`http://localhost:8080/api/hospitals/by-email?email=${encodeURIComponent(parsed.email)}`);
        const obj = await res.json(); setData(obj);
      } else if (parsed.role === 'BLOOD_BANK') {
        const res = await fetch(`http://localhost:8080/api/bloodbank/by-email?email=${encodeURIComponent(parsed.email)}`);
        const obj = await res.json(); setData(obj);
      }
    } catch (err) { console.error(err); }
    finally { setLoading(false); }
  };

  const handleChange = (e) => setData({ ...data, [e.target.name]: e.target.value });

  const [showLocationModal, setShowLocationModal] = useState(false);
  const [tempLocation, setTempLocation] = useState(null);

  const openLocationModal = () => {
    if (!data) return;
    const lat = data.latitude ? parseFloat(data.latitude) : null;
    const lng = data.longitude ? parseFloat(data.longitude) : null;
    setTempLocation(lat && lng ? { lat, lng, display_name: data.address || '' } : null);
    setShowLocationModal(true);
  };

  const closeLocationModal = () => {
    setShowLocationModal(false);
  };

  const saveLocationFromModal = async () => {
    if (!tempLocation || !data || !auth) return;
    setLoading(true);
    try {
      // prepare payload for address endpoint
      const payload = {
        address: tempLocation.display_name || data.address || '',
        latitude: Number(tempLocation.lat),
        longitude: Number(tempLocation.lng)
      };

      const base = auth.role === 'DONOR' ? 'http://localhost:8080/api/donors'
        : auth.role === 'HOSPITAL' ? 'http://localhost:8080/api/hospitals'
          : 'http://localhost:8080/api/bloodbank';
      const res = await fetch(`${base}/${data.id}/address`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (!res.ok) throw new Error('Failed to update location');
      const updated = await res.json();
      setData(updated);
      setMessage('Location saved');
      setTimeout(() => setMessage(''), 2500);
      setShowLocationModal(false);
    } catch (err) {
      console.error(err);
      setMessage('Location save failed');
      setTimeout(() => setMessage(''), 2500);
    } finally {
      setLoading(false);
    }
  };

  const save = async () => {
    if (!data) return;
    setLoading(true);
    try {
      const headers = { 'Content-Type': 'application/json' };
      if (auth.role === 'DONOR') {
        await fetch(`http://localhost:8080/api/donors/${data.id}`, { method: 'PUT', headers, body: JSON.stringify(data) });
      } else if (auth.role === 'HOSPITAL') {
        await fetch(`http://localhost:8080/api/hospitals/${data.id}`, { method: 'PUT', headers, body: JSON.stringify(data) });
      } else if (auth.role === 'BLOOD_BANK') {
        await fetch(`http://localhost:8080/api/bloodbank/${data.id}`, { method: 'PUT', headers, body: JSON.stringify(data) });
      }
      setMessage('Saved');
      setTimeout(() => setMessage(''), 2500);
    } catch (err) { setMessage('Save failed'); }
    finally { setLoading(false); }
  };

  const handleDonate = async () => {
    if (!donationForm.bloodBankId) { setMessage('Please select a blood bank'); return; }
    setLoading(true);
    try {
      const res = await fetch(`http://localhost:8080/api/donors/${data.id}/donate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(donationForm)
      });
      if (res.ok) {
        const updated = await res.json();
        setData(updated);
        setMessage('Donation successful! Your contribution is added.');
        setShowDonateForm(false);
        setTimeout(() => setMessage(''), 5000);
      } else {
        const err = await res.text();
        setMessage('Donation failed: ' + err);
      }
    } catch (err) { setMessage('Network error'); }
    finally { setLoading(false); }
  };

  if (!auth) return <div className="panel"><div className="panel-title">Not signed in</div></div>;

  return (
    <div className="panel">
      <div className="panel-header">
        <div className="panel-icon blue">👤</div>
        <div>
          <div className="panel-title">Profile</div>
          <div className="panel-desc">Manage your account</div>
        </div>
      </div>

      {loading && <div style={{ color: '#7a9bbf' }}>Loading…</div>}

      {data && (
        <div className="form-grid full">
          <div className="field span-2">
            <label>Full name</label>
            <input name="name" value={data.name || ''} onChange={handleChange} />
          </div>

          <div className="field">
            <label>Email Address</label>
            <input name="email" value={data.email || auth.email || ''} readOnly style={{ opacity: 0.7, cursor: "not-allowed" }} />
          </div>

          {auth.role === 'DONOR' ? (
            <>
              <div className="field">
                <label>Age</label>
                <input name="age" value={data.age || ''} readOnly />
              </div>
              <div className="field">
                <label>Blood Group</label>
                <select name="bloodGroup" value={data.bloodGroup || ''} onChange={handleChange}>
                  <option value="">-- Select --</option>
                  <option value="A+">A+</option>
                  <option value="A-">A-</option>
                  <option value="B+">B+</option>
                  <option value="B-">B-</option>
                  <option value="AB+">AB+</option>
                  <option value="AB-">AB-</option>
                  <option value="O+">O+</option>
                  <option value="O-">O-</option>
                </select>
              </div>
              <div className="field">
                <label>Last Donation</label>
                <input name="lastDonationDate" type="date" value={data.lastDonationDate || ''} onChange={handleChange} />
              </div>

              {/* Eligibility Status Section */}
              <div className="field span-2" style={{ marginTop: 8 }}>
                {(() => {
                  const lastDate = data.lastDonationDate;
                  if (!lastDate) {
                    return (
                      <div style={{ background: '#ecfdf5', color: '#059669', padding: '12px 16px', borderRadius: '12px', border: '1px solid #10b981', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600 }}>
                        <span></span> You are eligible to donate today!
                      </div>
                    );
                  }

                  const last = new Date(lastDate);
                  const today = new Date();
                  today.setHours(0, 0, 0, 0); // Normalize today
                  const diffTime = today - last;
                  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24));

                  if (diffDays >= 90) {
                    return (
                      <div style={{ background: '#ecfdf5', color: '#059669', padding: '12px 16px', borderRadius: '12px', border: '1px solid #10b981', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600 }}>
                        <span></span> You are eligible to donate today!
                      </div>
                    );
                  } else {
                    const remaining = 90 - diffDays;
                    return (
                      <div style={{ background: '#fff7ed', color: '#ea580c', padding: '12px 16px', borderRadius: '12px', border: '1px solid #f97316', display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600 }}>
                        <span></span> You need {remaining} more days to become eligible to donate.
                      </div>
                    );
                  }
                })()}
              </div>
            </>
          ) : (
            <div className="field">
              <label>Contact Number</label>
              <input name="contactNumber" value={data.contactNumber || ''} onChange={handleChange} />
            </div>
          )}

          <div className="field span-2">
            <label>Address</label>
            <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
              <div style={{ flex: 1 }}>{data.address || (data.latitude && data.longitude ? `${data.latitude}, ${data.longitude}` : 'No location set')}</div>
            </div>
            <button className="btn" type="button" onClick={openLocationModal}>Edit Location</button>
          </div>

        </div>
      )}

      <div style={{ display: 'flex', gap: 12, marginTop: 20 }}>
        <button className="btn btn-blue" onClick={save} disabled={loading}>Save Profile</button>
        {auth.role === 'HOSPITAL' && <button className="btn btn-red" onClick={() => window.location.href = '/request'}>Add Blood Request</button>}
        {auth.role === 'DONOR' && <button className="btn btn-red" onClick={() => setShowDonateForm(!showDonateForm)}>{showDonateForm ? 'Cancel Donation' : 'Donate Blood'}</button>}
      </div>

      {showDonateForm && auth.role === 'DONOR' && (
        <div style={{ marginTop: 32, padding: 24, border: '1px solid var(--border)', borderRadius: 16, background: '#f8fafc', boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.02)' }}>
          <h3 style={{ margin: '0 0 20px', color: '#dc2626', fontFamily: "'Syne', sans-serif", fontWeight: 800 }}>Register Your Donation</h3>
          <div className="form-grid">
            <div className="field">
              <label>Select Blood Bank</label>
              <select
                value={donationForm.bloodBankId}
                onChange={e => setDonationForm({ ...donationForm, bloodBankId: e.target.value })}
                style={{ background: 'white', border: '1px solid var(--border)', borderRadius: 8, padding: 12, color: 'var(--text-primary)', width: '100%', fontSize: 14 }}
              >
                <option value="">-- Choose Blood Bank --</option>
                {bloodBanks.map(bb => {
                  const parts = (bb.address || "").split(',').map(s => s.trim());
                  const pincode = parts.find(p => /\b\d{6}\b/.test(p)) || "";
                  const city = parts.length > 4 ? parts[parts.length - 5] : parts.length > 2 ? parts[parts.length - 3] : parts[0];
                  return (
                    <option key={bb.id} value={bb.id}>
                      {bb.name} ({city}{city && pincode ? ', ' : ''}{pincode})
                    </option>
                  );
                })}
              </select>
            </div>
            <div className="field">
              <label>Units Donated</label>
              <input type="number" min="1" value={donationForm.units} onChange={e => setDonationForm({ ...donationForm, units: parseInt(e.target.value) })} />
            </div>
            <div className="field">
              <label>Donation Date</label>
              <input type="date" value={donationForm.donationDate} onChange={e => setDonationForm({ ...donationForm, donationDate: e.target.value })} max={new Date().toISOString().split('T')[0]} />
            </div>
            <div className="field" style={{ display: 'flex', alignItems: 'flex-end' }}>
              <button className="btn btn-red" onClick={handleDonate} disabled={loading} style={{ width: '100%', marginTop: 0 }}>Confirm Donation</button>
            </div>
          </div>
        </div>
      )}

      {message && <div style={{ marginTop: 10 }}>{message}</div>}

      {showLocationModal && (
        <div
          className="location-modal-overlay"
          onClick={(e) => e.target === e.currentTarget && closeLocationModal()}
        >
          <div className="location-modal-box">

            {/* Header — needs z-index to stay above map */}
            <div style={{
              display: 'flex', alignItems: 'center', justifyContent: 'space-between',
              padding: '20px 24px', borderBottom: '1px solid var(--border)',
              position: 'relative', zIndex: 10, background: 'var(--bg-panel)',
              borderRadius: 'var(--radius-lg) var(--radius-lg) 0 0'
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div className="panel-icon blue">📍</div>
                <div>
                  <div className="panel-title" style={{ fontSize: '15px' }}>Select Location</div>
                  <div className="panel-desc">Search or pick on map</div>
                </div>
              </div>
              <button onClick={closeLocationModal} style={{
                width: '32px', height: '32px', borderRadius: 'var(--radius-sm)',
                background: 'var(--bg-input)', border: '1px solid var(--border)',
                color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '14px',
                display: 'flex', alignItems: 'center', justifyContent: 'center',
              }}>✕</button>
            </div>

            {/* Map — no padding wrapper, sits flush */}
            <div style={{ padding: '16px 24px', position: 'relative', zIndex: 1 }}>
              <MapPicker
                position={tempLocation ? [tempLocation.lat, tempLocation.lng] : null}
                onChange={(p) =>
                  setTempLocation(prev => ({
                    ...(prev || {}),
                    lat: p.lat,
                    lng: p.lng,
                    display_name: p.display_name || prev?.display_name || ''
                  }))
                }
              />
            </div>

            <div style={{
              padding: '10px 24px', borderTop: '1px solid var(--border)',
              background: 'var(--bg-input)', display: 'flex', alignItems: 'center', gap: '8px',
              position: 'relative', zIndex: 10
            }}>
              <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>📍</span>
              <span style={{
                fontSize: '13px',
                color: tempLocation ? 'var(--text-secondary)' : 'var(--text-muted)',
                overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', flex: 1
              }}>
                {tempLocation?.display_name
                  || (tempLocation ? `${tempLocation.lat.toFixed(5)}, ${tempLocation.lng.toFixed(5)}`
                    : 'No location selected')}
              </span>
            </div>

            <div style={{
              display: 'flex', gap: '10px', padding: '16px 24px',
              borderTop: '1px solid var(--border)', justifyContent: 'flex-end',
              position: 'relative', zIndex: 10,
              background: 'var(--bg-panel)',
              borderRadius: '0 0 var(--radius-lg) var(--radius-lg)'
            }}>
              <button onClick={closeLocationModal} style={{
                padding: '10px 20px', borderRadius: 'var(--radius-sm)',
                background: 'var(--bg-input)', border: '1px solid var(--border)',
                color: 'var(--text-secondary)', cursor: 'pointer', fontSize: '13px',
                fontFamily: "'Syne', sans-serif", fontWeight: 600, letterSpacing: '0.3px',
              }}>Cancel</button>
              <button onClick={saveLocationFromModal} disabled={!tempLocation} style={{
                padding: '10px 20px', borderRadius: 'var(--radius-sm)',
                background: tempLocation ? 'linear-gradient(135deg, #1d4ed8 0%, #3b82f6 100%)' : 'var(--bg-input)',
                border: tempLocation ? 'none' : '1px solid var(--border)',
                color: tempLocation ? 'white' : 'var(--text-muted)',
                cursor: tempLocation ? 'pointer' : 'not-allowed', fontSize: '13px',
                fontFamily: "'Syne', sans-serif", fontWeight: 600,
                boxShadow: tempLocation ? '0 4px 20px rgba(59,130,246,0.35)' : 'none',
              }}>Save Location</button>
            </div>

          </div>
        </div>
      )}
    </div>
  );
}

export default Profile;
