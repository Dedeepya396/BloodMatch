import { useState, useEffect } from "react";

function HospitalPendingRequests() {
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(1);
    const pageSize = 20;

    useEffect(() => {
        const auth = JSON.parse(localStorage.getItem("bm_auth"));
        if (!auth || auth.role !== 'HOSPITAL') return;

        const fetchPending = async () => {
            try {
                const res = await fetch(`http://localhost:8080/api/bank-requests/hospital/${encodeURIComponent(auth.name)}/pending`);
                if (res.ok) {
                    setRequests(await res.json());
                }
            } catch (err) {
                console.error("Failed to fetch pending requests", err);
            } finally {
                setLoading(false);
            }
        };

        fetchPending();
    }, []);

    const totalPages = Math.ceil(requests.length / pageSize);
    const startIndex = (currentPage - 1) * pageSize;
    const displayedRequests = requests.slice(startIndex, startIndex + pageSize);

    return (
        <div className="panel">
            <div className="panel-header">
                <div className="panel-icon orange">⏳</div>
                <div>
                    <div className="panel-title">Pending Requests</div>
                    <div className="panel-desc">Requests currently waiting for blood bank approval</div>
                </div>
            </div>

            {loading ? (
                <div className="empty-state">Loading pending requests...</div>
            ) : requests.length === 0 ? (
                <div className="empty-state">No pending requests at the moment.</div>
            ) : (
                <>
                    <div className="matches-list">
                        {displayedRequests.map((req, index) => (
                            <div className="match-item" key={req.id} style={{ animationDelay: `${index * 0.05}s` }}>
                                <div className="match-info">
                                    <div className="match-name">{req.hospitalName} ➔ {req.bloodGroup}</div>
                                    <div className="match-meta">
                                        Requested: {req.unitsRequested} units ·
                                        Status: <span style={{ color: '#f59e0b', fontWeight: 600 }}>{req.status}</span>
                                    </div>
                                </div>
                                <div className="blood-badge">{req.bloodGroup}</div>
                            </div>
                        ))}
                    </div>

                    {totalPages > 1 && (
                        <div className="pagination">
                            {[...Array(totalPages)].map((_, i) => (
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
        </div>
    );
}

export default HospitalPendingRequests;
