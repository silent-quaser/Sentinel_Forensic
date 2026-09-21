import React from 'react';
import { MessageSquarePlus } from 'lucide-react';
import { InvestigationNote } from '../../types';

interface InvestigationNotesTabProps {
  notes: InvestigationNote[];
  onOpenAddNote: () => void;
}

export const InvestigationNotesTab: React.FC<InvestigationNotesTabProps> = ({
  notes,
  onOpenAddNote,
}) => {
  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between pb-2">
        <div>
          <h3 className="text-base font-bold text-white flex items-center gap-2">
            <MessageSquarePlus className="text-cyan-400" size={18} />
            <span>Forensic Case Dossier Notes ({notes.length})</span>
          </h3>
          <p className="text-xs text-slate-400 mt-0.5">
            Official commentary, working hypotheses, and investigative findings.
          </p>
        </div>
        <button
          onClick={onOpenAddNote}
          className="px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold flex items-center gap-2 transition-all cursor-pointer"
        >
          <MessageSquarePlus size={14} />
          <span>Add Note</span>
        </button>
      </div>

      <div className="space-y-4">
        {notes.map((note) => (
          <div key={note.id} className="p-5 rounded-2xl bg-slate-900/90 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <div className="w-6 h-6 rounded-full bg-cyan-950 border border-cyan-500/40 flex items-center justify-center text-cyan-400 font-bold text-xs font-mono">
                  {note.author.charAt(0)}
                </div>
                <span className="text-xs font-bold font-mono text-white">{note.author}</span>
              </div>
              <span className="text-[11px] font-mono text-slate-500">
                {note.createdAt ? new Date(note.createdAt).toLocaleString() : ''}
              </span>
            </div>
            <p className="text-xs text-slate-300 leading-relaxed pl-8">{note.content}</p>
          </div>
        ))}

        {notes.length === 0 && (
          <div className="p-12 text-center rounded-2xl bg-slate-900/60 border border-slate-800 text-xs text-slate-500 font-mono">
            No investigative notes recorded for this case. Click "Add Note" above to add observations.
          </div>
        )}
      </div>
    </div>
  );
};
