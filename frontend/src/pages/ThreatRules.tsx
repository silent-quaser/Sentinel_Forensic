import React, { useEffect, useState } from 'react';
import { Sliders, CheckCircle2, XCircle, Edit3 } from 'lucide-react';
import { ruleApi } from '../api/client';
import { ThreatRule } from '../types';
import { SeverityBadge } from '../components/common/SeverityBadge';
import { RuleEditModal } from '../components/common/RuleEditModal';

export default function ThreatRules() {
  const [rules, setRules] = useState<ThreatRule[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedRule, setSelectedRule] = useState<ThreatRule | null>(null);

  const loadRules = async () => {
    try {
      const data = await ruleApi.getAll();
      setRules(data);
    } catch (err) {
      console.error('Failed to load rules', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRules();
  }, []);

  const handleToggle = async (rule: ThreatRule) => {
    try {
      const updated = await ruleApi.update(rule.id, { enabled: !rule.enabled });
      setRules((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
    } catch (err) {
      alert('Failed to toggle rule state');
    }
  };

  return (
    <div className="space-y-6 pb-12">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-white tracking-tight flex items-center gap-2">
            <Sliders className="text-cyan-400" size={24} />
            <span>Detection Rule Catalog</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Configure detection thresholds, sliding time windows, and active state for the 7 built-in forensic heuristic rules.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {rules.map((rule) => (
          <div
            key={rule.id}
            className={`p-6 rounded-2xl border transition-all ${
              rule.enabled
                ? 'bg-slate-900/90 border-slate-800 hover:border-slate-700 shadow-lg'
                : 'bg-slate-950/40 border-slate-800/60 opacity-60'
            }`}
          >
            <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4">
              <div className="space-y-2">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="font-mono text-xs text-cyan-400 bg-cyan-950/60 px-2.5 py-1 rounded-md border border-cyan-500/30">
                    {rule.ruleCode}
                  </span>
                  <SeverityBadge severity={rule.severity} size="sm" />
                  <span className={`px-2 py-0.5 rounded-full text-[11px] font-mono font-medium border ${
                    rule.enabled
                      ? 'bg-emerald-950/50 text-emerald-400 border-emerald-500/30'
                      : 'bg-slate-800 text-slate-400 border-slate-700'
                  }`}>
                    {rule.enabled ? 'ACTIVE ENGINE RULE' : 'DISABLED'}
                  </span>
                </div>

                <h3 className="text-lg font-bold text-white">{rule.name}</h3>
                <p className="text-xs text-slate-300 leading-relaxed max-w-3xl">{rule.description}</p>
              </div>

              <div className="flex items-center gap-2 self-start sm:self-auto shrink-0">
                <button
                  onClick={() => setSelectedRule(rule)}
                  className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer"
                >
                  <Edit3 size={14} className="text-cyan-400" />
                  <span>Configure</span>
                </button>
                <button
                  onClick={() => handleToggle(rule)}
                  className={`px-3.5 py-2 rounded-xl text-xs font-semibold flex items-center gap-1.5 transition-all cursor-pointer ${
                    rule.enabled
                      ? 'bg-red-950/50 hover:bg-red-900/60 text-red-300 border border-red-500/30'
                      : 'bg-emerald-950/50 hover:bg-emerald-900/60 text-emerald-300 border border-emerald-500/30'
                  }`}
                >
                  {rule.enabled ? <XCircle size={14} /> : <CheckCircle2 size={14} />}
                  <span>{rule.enabled ? 'Disable' : 'Enable'}</span>
                </button>
              </div>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-4 pt-4 border-t border-slate-800/80 text-xs font-mono">
              <div className="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 block text-[10px] uppercase font-sans">Event Threshold</span>
                <span className="text-cyan-300 font-bold text-sm">&gt;= {rule.threshold}</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 block text-[10px] uppercase font-sans">Sliding Time Window</span>
                <span className="text-white font-bold text-sm">{rule.timeWindowMinutes} min ({rule.timeWindowSeconds}s)</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 block text-[10px] uppercase font-sans">Target Category</span>
                <span className="text-slate-300 font-bold text-sm">Authentication & Access</span>
              </div>
              <div className="p-2.5 rounded-lg bg-slate-950/60 border border-slate-800">
                <span className="text-slate-500 block text-[10px] uppercase font-sans">Engine Implementation</span>
                <span className="text-emerald-400 font-bold text-sm">Deterministic Heuristic</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {selectedRule && (
        <RuleEditModal
          rule={selectedRule}
          onClose={() => setSelectedRule(null)}
          onSaved={(updated) => {
            setRules((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
          }}
        />
      )}
    </div>
  );
}
