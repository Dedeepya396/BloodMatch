import { useEffect, useState } from "react";

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

          <div className="field">
            <label>Latitude</label>
            <input name="latitude" value={data.latitude||''} onChange={handleChange} />
          </div>
          <div className="field">
            <label>Longitude</label>
            <input name="longitude" value={data.longitude||''} onChange={handleChange} />
          </div>

        </div>
      )}

      <div style={{display:'flex', gap:12}}>
        <button className="btn btn-blue" onClick={save} disabled={loading}>Save Profile</button>
        {auth.role === 'HOSPITAL' && <button className="btn btn-red" onClick={() => window.location.href = '/request'}>Add Blood Request</button>}
      </div>

      {message && <div style={{marginTop:10}}>{message}</div>}
    </div>
  );
}

export default Profile;
