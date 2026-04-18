import { useEffect, useState } from "react";
import MapPicker from "./MapPicker";

function Profile() {
  const [auth, setAuth] = useState(null);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    const raw = localStorage.getItem('bm_auth');
    if (!raw) return;
    const parsed = JSON.parse(raw);
    setAuth(parsed);
    fetchProfile(parsed);
  }, []);

  const fetchProfile = async (parsed) => {
    setLoading(true);
    try {
      if (parsed.role === 'DONOR') {
        const res = await fetch(`http://localhost:8080/api/donors/by-email?email=${encodeURIComponent(parsed.email)}`);
        const obj = await res.json(); setData(obj);
      } else if (parsed.role === 'HOSPITAL') {
        const res = await fetch(`http://localhost:8080/api/hospitals/by-email?email=${encodeURIComponent(parsed.email)}`);
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

      const base = auth.role === 'DONOR' ? 'http://localhost:8080/api/donors' : 'http://localhost:8080/api/hospitals';
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
      const headers = { 'Content-Type':'application/json' };
      if (auth.role === 'DONOR') {
        await fetch(`http://localhost:8080/api/donors/${data.id}`, { method:'PUT', headers, body: JSON.stringify(data) });
      } else {
        await fetch(`http://localhost:8080/api/hospitals/${data.id}`, { method:'PUT', headers, body: JSON.stringify(data) });
      }
      setMessage('Saved');
      setTimeout(() => setMessage(''), 2500);
    } catch (err) { setMessage('Save failed'); }
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

      {loading && <div style={{color:'#7a9bbf'}}>Loading…</div>}

      {data && (
        <div className="form-grid full">
          <div className="field">
            <label>Full name</label>
            <input name="name" value={data.name||''} onChange={handleChange} />
          </div>

          {auth.role === 'DONOR' ? (
            <>
              <div className="field">
                <label>Blood Group</label>
                <input name="bloodGroup" value={data.bloodGroup||''} onChange={handleChange} />
              </div>
              <div className="field">
                <label>Last Donation</label>
                <input name="lastDonationDate" type="date" value={data.lastDonationDate||''} onChange={handleChange} />
              </div>
            </>
          ) : (
            <>
              <div className="field">
                <label>Address</label>
                <input name="address" value={data.address||''} onChange={handleChange} />
              </div>
              <div className="field">
                <label>Contact Number</label>
                <input name="contactNumber" value={data.contactNumber||''} onChange={handleChange} />
              </div>
            </>
          )}

          <div className="field span-2">
            <label>Address</label>
            <div style={{display:'flex',gap:8,alignItems:'center'}}>
              <div style={{flex:1}}>{data.address || (data.latitude && data.longitude ? `${data.latitude}, ${data.longitude}` : 'No location set')}</div>
            </div>
              <button className="btn" type="button" onClick={openLocationModal}>Edit Location</button>
          </div>

        </div>
      )}

      <div style={{display:'flex', gap:12}}>
        <button className="btn btn-blue" onClick={save} disabled={loading}>Save Profile</button>
        {auth.role === 'HOSPITAL' && <button className="btn btn-red" onClick={() => window.location.href = '/request'}>Add Blood Request</button>}
      </div>

      {message && <div style={{marginTop:10}}>{message}</div>}

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
