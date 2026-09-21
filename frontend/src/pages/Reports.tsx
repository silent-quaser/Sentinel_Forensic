import React, { useEffect, useState } from 'react';
import { FileBarChart2, FileDown, ExternalLink, Calendar, User } from 'lucide-react';
import { investigationApi } from '../api/client';
import { Investigation } from '../types';
import { StatusBadge } from '../components/common/StatusBadge';
import { Link } from 'react-router-dom';

export default function Reports() {
  const [investigations, setInvestigations] = useState<Investigation[]>([]);
  const [loading, setLoading] = useState(true);
  const [downloadingId, setDownloadingId] = useState<number | null>(null);

  useEffect(() => {
    investigationApi.getAll().then((data) => {
      setInvestigations(data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const handleDownload = async (id: number, invCode: string) => {
    setDownloadingId(id);
    try {
      await investigationApi.downloadPdfReport(id, `SentinelForensic_Report_${invCode}.pdf`);
    } catch (err) {
      alert('Failed to generate forensic PDF report');
    } finally {
      setDownloadingId(null);
    }
  };

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <FileBarChart2 className="text-cyan-400" size={24} />
            <span>Forensic Incident Reports Center</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Download official court-ready / executive incident response reports produced with Apache PDFBox 3.x.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {investigations.map((inv) => (
          <div
            key={inv.id}
            className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm space-y-4 hover:border-slate-700 transition-all flex flex-col justify-between"
          >
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <span className="font-mono text-xs font-bold text-cyan-400 bg-cyan-950/60 px-2.5 py-1 rounded-md border border-cyan-500/30">
                  {inv.investigationId}
                </span>
                <StatusBadge status={inv.status} />
              </div>

              <div>
                <h3 className="text-base font-bold text-white">{inv.name}</h3>
                <p className="text-xs text-slate-400 mt-1 line-clamp-2">{inv.description || 'No description provided.'}</p>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs font-mono text-slate-300 pt-2">
                <div className="flex items-center gap-1.5">
                  <User size={13} className="text-slate-500" />
                  <span>Lead: {inv.investigatorName}</span>
                </div>
                <div className="flex items-center gap-1.5">
                  <Calendar size={13} className="text-slate-500" />
                  <span>{inv.createdAt ? new Date(inv.createdAt).toLocaleDateString() : 'N/A'}</span>
                </div>
              </div>
            </div>

            <div className="pt-4 border-t border-slate-800/80 flex items-center justify-between gap-3">
              <Link
                to={`/investigations/${inv.id}`}
                className="text-xs text-slate-400 hover:text-cyan-400 flex items-center gap-1 font-mono transition-colors"
              >
                <span>Examine Dossier</span>
                <ExternalLink size={13} />
              </Link>

              <button
                onClick={() => handleDownload(inv.id, inv.investigationId)}
                disabled={downloadingId === inv.id}
                className="px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold flex items-center gap-2 shadow-[0_0_12px_rgba(6,182,212,0.3)] transition-all cursor-pointer disabled:opacity-50"
              >
                <FileDown size={14} />
                <span>{downloadingId === inv.id ? 'Generating...' : 'Export PDF'}</span>
              </button>
            </div>
          </div>
        ))}

        {investigations.length === 0 && !loading && (
          <div className="col-span-2 p-12 text-center rounded-2xl bg-slate-900/60 border border-slate-800 text-xs text-slate-500 font-mono">
            No investigation dossiers available. Create an investigation to generate forensic reports.
          </div>
        )}
      </div>
    </div>
  );
}
