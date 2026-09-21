import React, { useEffect, useState } from 'react';
import { ShieldAlert, AlertOctagon, RefreshCw, ChevronRight } from 'lucide-react';
import { threatApi } from '../api/client';
import { Threat } from '../types';
import { SeverityBadge } from '../components/common/SeverityBadge';
import { StatusBadge } from '../components/common/StatusBadge';
import { ScoreBadge } from '../components/common/ScoreBadge';
import { ThreatDetailDrawer } from '../components/common/ThreatDetailDrawer';

export default function GlobalThreats() {
  const [threats, setThreats] = useState<Threat[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedThreat, setSelectedThreat] = useState<Threat | null>(null);

  const [severityFilter, setSeverityFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const loadThreats = async () => {
    setLoading(true);
    try {
      const data = await threatApi.getAllThreats({
        severity: severityFilter || undefined,
        status: statusFilter || undefined,
      });
      setThreats(data);
    } catch (err) {
      console.error('Failed to load global threats', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadThreats();
  }, [severityFilter, statusFilter]);

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <AlertOctagon className="text-red-400" size={24} />
            <span>Threat Intelligence Center</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Correlated detections across all forensic cases, scoring formula evaluations, and escalation audits.
          </p>
        </div>

        <button
          onClick={loadThreats}
          className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium flex items-center gap-2 border border-slate-700 transition-all cursor-pointer self-start sm:self-auto"
        >
          <RefreshCw size={14} className={loading ? 'animate-spin text-cyan-400' : ''} />
          <span>Refresh Threats</span>
        </button>
      </div>

      <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-4 shadow-lg flex flex-wrap items-center justify-between gap-4">
        <div className="flex items-center gap-3 text-xs flex-wrap">
          <span className="text-slate-500 font-mono text-[11px]">SEVERITY:</span>
          {['', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map((s) => (
            <button
              key={s || 'ALL'}
              onClick={() => setSeverityFilter(s)}
              className={`px-3 py-1.5 rounded-lg font-mono text-[11px] transition-all cursor-pointer ${
                severityFilter === s
                  ? 'bg-cyan-500/20 text-cyan-300 border border-cyan-500/40 font-semibold'
                  : 'bg-slate-950 text-slate-400 border border-slate-800 hover:text-white'
              }`}
            >
              {s || 'ALL'}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2 text-xs">
          <span className="text-slate-500 font-mono text-[11px]">STATUS:</span>
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="bg-slate-950 border border-slate-700 rounded-xl px-3 py-1.5 text-slate-300 font-mono outline-none"
          >
            <option value="">ALL STATUSES</option>
            <option value="DETECTED">DETECTED</option>
            <option value="REVIEWED">REVIEWED</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="FALSE_POSITIVE">FALSE POSITIVE</option>
            <option value="RESOLVED">RESOLVED</option>
          </select>
        </div>
      </div>

      <div className="space-y-4">
        {threats.map((threat) => (
          <div
            key={threat.id}
            className="p-6 rounded-2xl bg-slate-900/90 border border-slate-800 hover:border-slate-700 shadow-xl transition-all space-y-4 backdrop-blur-sm"
          >
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-3">
              <div className="space-y-1.5">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-mono text-xs text-cyan-400 bg-cyan-950/60 px-2.5 py-0.5 rounded border border-cyan-500/30">
                    {threat.ruleCode}
                  </span>
                  <span className="font-mono text-[11px] text-slate-400 bg-slate-950 px-2 py-0.5 rounded border border-slate-800">
                    Dossier #{threat.investigationId}
                  </span>
                  <SeverityBadge severity={threat.severity} size="sm" />
                  <ScoreBadge score={threat.score} />
                  <StatusBadge status={threat.status} />
                </div>
                <h3 className="text-lg font-bold text-white">{threat.title}</h3>
                <p className="text-xs text-slate-300 leading-relaxed">{threat.description}</p>
              </div>

              <button
                onClick={() => setSelectedThreat(threat)}
                className="px-4 py-2 rounded-xl bg-slate-800 hover:bg-cyan-600 hover:text-white text-slate-200 text-xs font-semibold transition-all flex items-center gap-1.5 self-start sm:self-auto cursor-pointer shrink-0"
              >
                <span>Examine Causative Evidence</span>
                <ChevronRight size={14} />
              </button>
            </div>

            {threat.escalationReason && (
              <div className="p-3.5 rounded-xl bg-red-950/40 border border-red-500/40 flex items-start gap-3">
                <ShieldAlert size={18} className="text-red-400 shrink-0 mt-0.5" />
                <div className="text-xs">
                  <span className="font-semibold text-red-300 block">Severity Escalation Override</span>
                  <span className="font-mono text-red-200">{threat.escalationReason}</span>
                  <div className="mt-1 text-[11px] text-red-300/80">
                    Detection Score: <span className="font-mono font-bold text-white">{threat.score}</span> &bull; Escalated to <span className="font-bold text-red-400">CRITICAL</span> without score inflation.
                  </div>
                </div>
              </div>
            )}

            {threat.scoreBreakdown && (
              <div className="p-3 rounded-xl bg-slate-950/60 border border-slate-800 text-xs font-mono text-slate-300 flex items-center justify-between">
                <span className="text-slate-500 uppercase text-[10px]">Scoring Formula</span>
                <span className="text-cyan-300 font-semibold">{threat.scoreBreakdown}</span>
              </div>
            )}

            <div className="flex items-center justify-between text-xs text-slate-400 pt-2 border-t border-slate-800/80">
              <span>Subject: <span className="font-mono text-white">{threat.affectedUser || 'N/A'}</span> &bull; Detected: {threat.detectedAt}</span>
              <span className="font-mono text-cyan-400">{threat.evidenceEvents?.length || 0} Linked Evidence Record(s)</span>
            </div>
          </div>
        ))}

        {threats.length === 0 && (
          <div className="p-12 text-center rounded-2xl bg-slate-900/60 border border-slate-800 text-xs text-slate-500 font-mono">
            {loading ? 'Querying threat database...' : 'No threats found matching selected filter criteria.'}
          </div>
        )}
      </div>

      {selectedThreat && (
        <ThreatDetailDrawer
          threat={selectedThreat}
          onClose={() => setSelectedThreat(null)}
          onStatusUpdated={(updated) => {
            setSelectedThreat(updated);
            loadThreats();
          }}
        />
      )}
    </div>
  );
}
