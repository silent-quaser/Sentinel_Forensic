import React, { useState } from 'react';
import { X, MessageSquarePlus } from 'lucide-react';
import { InvestigationNote } from '../../types';
import { investigationApi } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

interface AddNoteModalProps {
  investigationId: number;
  isOpen: boolean;
  onClose: () => void;
  onNoteAdded: (note: InvestigationNote) => void;
}

export const AddNoteModal: React.FC<AddNoteModalProps> = ({
  investigationId,
  isOpen,
  onClose,
  onNoteAdded,
}) => {
  const { user } = useAuth();
  const [content, setContent] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!content.trim()) return;

    setSubmitting(true);
    setError(null);
    try {
      const note = await investigationApi.addNote(
        investigationId,
        user?.fullName || 'Naveen (Investigator)',
        content.trim()
      );
      onNoteAdded(note);
      setContent('');
      onClose();
    } catch (err: any) {
      setError(err?.message || 'Failed to add note');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
      <div className="bg-slate-900 border border-slate-700 rounded-xl w-full max-w-lg overflow-hidden shadow-2xl animate-in fade-in zoom-in duration-150">
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/50">
          <div className="flex items-center gap-2 text-cyan-400 font-mono text-sm font-semibold">
            <MessageSquarePlus size={18} />
            <span>ADD INVESTIGATION NOTE</span>
          </div>
          <button
            onClick={onClose}
            className="text-slate-400 hover:text-white p-1 rounded hover:bg-slate-800 transition-colors"
          >
            <X size={18} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          {error && (
            <div className="p-3 bg-red-950/50 border border-red-500/40 rounded text-red-300 text-xs">
              {error}
            </div>
          )}

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Investigator Author
            </label>
            <input
              type="text"
              disabled
              value={user?.fullName || 'Naveen (Investigator)'}
              className="w-full bg-slate-950/60 border border-slate-800 rounded-lg px-3 py-2 text-xs text-slate-400 font-mono"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Case Observation / Forensic Note
            </label>
            <textarea
              required
              rows={5}
              value={content}
              onChange={(e) => setContent(e.target.value)}
              placeholder="Enter forensic observations, correlation rationale, hypothesis, or triage notes..."
              className="w-full bg-slate-950 border border-slate-700 rounded-lg p-3 text-sm text-white placeholder-slate-500 focus:border-cyan-400 focus:outline-none resize-none"
            />
          </div>

          <div className="pt-3 border-t border-slate-800 flex justify-end gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-lg transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={submitting || !content.trim()}
              className="px-4 py-2 text-xs font-semibold bg-cyan-600 hover:bg-cyan-500 text-white rounded-lg transition-colors disabled:opacity-50"
            >
              {submitting ? 'Saving...' : 'Add Note to Dossier'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
