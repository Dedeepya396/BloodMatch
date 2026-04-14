import { useState } from "react";
import { useNavigate } from "react-router-dom";

function Signup() {
    const [role, setRole] = useState('DONOR');
    const [form, setForm] = useState({ name: '', email: '', password: '', bloodGroup: '', latitude: '', longitude: '', lastDonationDate: '', address: '', contactNumber: '' });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

    const submit = async () => {
        setLoading(true); setError('');
        try {
            const payload = { role, name: form.name, email: form.email, password: form.password };
            if (role === 'DONOR') {
                payload.bloodGroup = form.bloodGroup; payload.latitude = parseFloat(form.latitude || 0); payload.longitude = parseFloat(form.longitude || 0); payload.available = true;
                if (form.lastDonationDate) {
                    payload.lastDonationDate = form.lastDonationDate;
                }
            } else {
                payload.address = form.address; payload.contactNumber = form.contactNumber; payload.latitude = parseFloat(form.latitude || 0); payload.longitude = parseFloat(form.longitude || 0);
            }
            const res = await fetch('http://localhost:8080/api/auth/signup', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
            if (!res.ok) { const txt = await res.text(); setError(txt || 'Signup failed'); return; }
            const data = await res.json();
            // store basic auth info (no JWT)
            localStorage.setItem('bm_auth', JSON.stringify({ id: data.id, role: data.role, name: data.name, email: data.email }));
            if (data.role === 'DONOR') navigate('/profile'); else navigate('/request');
        } catch (err) { setError('Network error'); }
        finally { setLoading(false); }
    };

    return (
        <div className="panel">
            <div className="panel-header">
                <div className="panel-icon blue">✍️</div>
                <div>
                    <div className="panel-title">Sign Up</div>
                    <div className="panel-desc">Create a new account</div>
                </div>
            </div>

            <div style={{ marginBottom: 12 }}>
                <label style={{ fontSize: 12, color: '#7a9bbf' }}>Role</label>
                <div style={{ display: 'flex', gap: 8, marginTop: 6 }}>
                    <button className={`btn ${role === 'DONOR' ? 'btn-blue' : ''}`} onClick={() => setRole('DONOR')} type="button">Donor</button>
                    <button className={`btn ${role === 'HOSPITAL' ? 'btn-blue' : ''}`} onClick={() => setRole('HOSPITAL')} type="button">Hospital</button>
                </div>
            </div>

            <div className="form-grid full">
                <div className="field">
                    <label>Full name</label>
                    <input name="name" value={form.name} onChange={handleChange} />
                </div>
                <div className="field">
                    <label>Email</label>
                    <input name="email" value={form.email} onChange={handleChange} />
                </div>
                <div className="field">
                    <label>Password</label>
                    <input name="password" type="password" value={form.password} onChange={handleChange} />
                </div>

                {role === 'DONOR' ? (
                    <>
                        <div className="field">
                            <label>Blood Group</label>
                            <input name="bloodGroup" value={form.bloodGroup} onChange={handleChange} placeholder="e.g. A+" />
                        </div>
                        <div className="field">
                            <label>Last Donation</label>
                            <input name="lastDonationDate" type="date" value={form.lastDonationDate} onChange={handleChange} />
                        </div>
                    </>
                ) : (
                    <>
                        <div className="field">
                            <label>Address</label>
                            <input name="address" value={form.address} onChange={handleChange} />
                        </div>
                        <div className="field">
                            <label>Contact Number</label>
                            <input name="contactNumber" value={form.contactNumber} onChange={handleChange} />
                        </div>
                    </>
                )}

                <div className="field">
                    <label>Latitude</label>
                    <input name="latitude" value={form.latitude} onChange={handleChange} />
                </div>
                <div className="field">
                    <label>Longitude</label>
                    <input name="longitude" value={form.longitude} onChange={handleChange} />
                </div>
            </div>

            {error && <div style={{ color: '#fb7185', marginTop: 8 }}>{error}</div>}

            <button className="btn btn-blue" onClick={submit} disabled={loading}>{loading ? 'Signing up…' : 'Create Account'}</button>
        </div>
    );
}

export default Signup;
