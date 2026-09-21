import React, { useState } from 'react';
import { X, Sliders } from 'lucide-react';
import { ThreatRule } from '../../types';
import { ruleApi } from '../../api/client';

interface RuleEditModalProps {
  rule: ThreatRule | null;
  onClose: () => void;
  onSaved: (updatedRule: ThreatRule) => void;
}

export const RuleEditModal: React.FC<RuleEditModalProps> = ({ rule, onClose, onSaved }) => {
  const [threshold, setThreshold] = useState<number>(rule?.threshold || 3);
  const [windowMinutes, setWindowMinutes] = useState<number>(rule?.timeWindowMinutes || 5);
  const [enabled, setEnabled] = useState<boolean>(rule?.enabled ?? true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!rule) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const updated = await ruleApi.update(rule.id, {
        enabled,
        thresholdCount: threshold,
        windowMinutes: windowMinutes,
      });
      onSaved(updated);
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to update rule');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-xl w-full max-w-lg overflow-hidden shadow-2xl animate-in fade-in zoom-in duration-150">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/50">
          <div className="flex items-center gap-2 text-cyan-400 font-mono text-sm font-semibold">
            <Sliders size={18} />
            <span>CONFIGURE DETECTION RULE [{rule.ruleCode}]</span>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded hover:bg-slate-800 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          <div>
            <h3 className="text-base font-bold text-white">{rule.name}</h3>
            <p className="text-xs text-slate-400 mt-1">{rule.description}</p>
          </div>

          {error && (
            <div className="p-3 bg-red-950/50 border border-red-500/40 rounded text-red-300 text-xs">
              {error}
            </div>
          )}

          <div className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Threshold Count
              </label>
              <input
                type="number"
                min="1"
                max="1000"
                value={threshold}
                onChange={(e) => setThreshold(parseInt(e.target.value) || 1)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white font-mono focus:border-cyan-400 focus:outline-none"
              />
              <span className="text-[11px] text-slate-500 mt-1 block">
                Minimum occurrences required to trigger threat alert.
              </span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Sliding Time Window (Minutes)
              </label>
              <input
                type="number"
                min="1"
                max="1440"
                value={windowMinutes}
                onChange={(e) => setWindowMinutes(parseInt(e.target.value) || 1)}
                className="w-full bg-slate-950 border border-slate-700 rounded-lg px-3 py-2 text-sm text-white font-mono focus:border-cyan-400 focus:outline-none"
              />
              <span className="text-[11px] text-slate-500 mt-1 block">
                Maximum interval across events for correlation.
              </span>
            </div>

            <div className="flex items-center justify-between p-3 rounded-lg bg-slate-950/60 border border-slate-800">
              <div>
                <span className="text-xs font-semibold text-slate-200 block">Engine Active Status</span>
                <span className="text-[11px] text-slate-500">Evaluate this rule during automated detections</span>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={enabled}
                  onChange={(e) => setEnabled(e.target.checked)}
                  className="sr-only peer"
                />
                <div className="w-11 h-6 bg-slate-800 peer-focus:outline-none rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-cyan-500"></div>
              </label>
            </div>
          </div>

          <div className="pt-4 border-t border-slate-800 flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-4 py-2 text-xs font-semibold bg-cyan-600 hover:bg-cyan-500 text-white rounded-lg flex items-center gap-1.5 transition-colors disabled:opacity-50"
            >
              {saving ? 'Updating...' : 'Save Parameters'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
