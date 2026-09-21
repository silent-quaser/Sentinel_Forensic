import React from 'react';
import { Calendar, User, Play, UploadCloud, FileDown, MessageSquarePlus, Info, CheckCircle2 } from 'lucide-react';
import { Investigation } from '../../types';
import { StatusBadge } from '../common/StatusBadge';

interface InvestigationHeaderProps {
  investigation: Investigation;
  timelineCount: number;
  threatCount: number;
  runningDetection: boolean;
  downloadingPdf: boolean;
  detectionNotice: string | null;
  uploadNotice: string | null;
  onRunDetection: () => void;
  onOpenUpload: () => void;
  onDownloadPdf: () => void;
  onOpenAddNote: () => void;
  onStatusChange: (newStatus: string) => void;
}

export const InvestigationHeader: React.FC<InvestigationHeaderProps> = ({
  investigation,
  timelineCount,
  threatCount,
  runningDetection,
  downloadingPdf,
  detectionNotice,
  uploadNotice,
  onRunDetection,
  onOpenUpload,
  onDownloadPdf,
  onOpenAddNote,
  onStatusChange,
}) => {
  return (
    <div className="bg-slate-900/90 border border-slate-800 rounded-2xl p-6 shadow-xl backdrop-blur-sm">
      <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-6">
        <div className="space-y-2 min-w-0">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-mono text-xs font-bold text-cyan-400 bg-cyan-950/60 px-2.5 py-1 rounded-md border border-cyan-500/30">
              {investigation.investigationId}
            </span>
            <StatusBadge status={investigation.status} />
            <span className="text-[11px] text-slate-500 font-mono flex items-center gap-1">
              <Calendar size={12} />
              {investigation.createdAt ? new Date(investigation.createdAt).toLocaleString() : 'N/A'}
            </span>
          </div>

          <h1 className="text-2xl lg:text-3xl font-bold text-white tracking-tight leading-snug">
            {investigation.name}
          </h1>

          <div className="flex items-center gap-4 text-xs text-slate-400 flex-wrap">
            <div className="flex items-center gap-1.5">
              <User size={14} className="text-cyan-400" />
              <span className="text-slate-300 font-mono">Lead: {investigation.investigatorName}</span>
            </div>
            <span>&bull;</span>
            <span>Classification: <span className="font-mono text-slate-300">SOC Triage Level 3</span></span>
            <span>&bull;</span>
            <span>Events: <span className="font-mono font-bold text-cyan-400">{timelineCount}</span></span>
            <span>&bull;</span>
            <span>Threats: <span className="font-mono font-bold text-red-400">{threatCount}</span></span>
          </div>
        </div>

        <div className="flex items-center gap-2.5 flex-wrap shrink-0">
          <button
            onClick={onRunDetection}
            disabled={runningDetection}
            className="px-3.5 py-2 rounded-xl bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 hover:to-rose-500 text-white text-xs font-semibold flex items-center gap-2 shadow-[0_0_15px_rgba(239,68,68,0.3)] transition-all cursor-pointer disabled:opacity-50"
          >
            <Play size={14} className={runningDetection ? 'animate-spin' : ''} />
            <span>{runningDetection ? 'Analyzing...' : 'Run Detection'}</span>
          </button>

          <button
            onClick={onOpenUpload}
            className="px-3.5 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 text-xs font-medium flex items-center gap-2 border border-slate-700 transition-all cursor-pointer"
          >
            <UploadCloud size={14} className="text-cyan-400" />
            <span>Upload Logs</span>
          </button>

          <button
            onClick={onDownloadPdf}
            disabled={downloadingPdf}
            className="px-3.5 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold flex items-center gap-2 shadow-[0_0_12px_rgba(6,182,212,0.3)] transition-all cursor-pointer disabled:opacity-50"
          >
            <FileDown size={14} />
            <span>{downloadingPdf ? 'Generating PDF...' : 'Forensic Report PDF'}</span>
          </button>

          <button
            onClick={onOpenAddNote}
            className="px-3 py-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-medium flex items-center gap-1.5 border border-slate-700 transition-all cursor-pointer"
          >
            <MessageSquarePlus size={14} />
            <span>Add Note</span>
          </button>

          <select
            value={investigation.status}
            onChange={(e) => onStatusChange(e.target.value)}
            className="bg-slate-950 text-slate-300 border border-slate-700 rounded-xl px-3 py-2 text-xs font-mono outline-none cursor-pointer hover:border-slate-600"
          >
            <option value="OPEN">STATUS: OPEN</option>
            <option value="IN_PROGRESS">STATUS: IN PROGRESS</option>
            <option value="CLOSED">STATUS: CLOSED</option>
            <option value="ARCHIVED">STATUS: ARCHIVED</option>
          </select>
        </div>
      </div>

      {detectionNotice && (
        <div className="mt-4 p-3 rounded-xl bg-cyan-950/60 border border-cyan-500/40 text-cyan-300 text-xs font-mono flex items-center gap-2">
          <Info size={16} className="shrink-0" />
          <span>{detectionNotice}</span>
        </div>
      )}
      {uploadNotice && (
        <div className="mt-4 p-3 rounded-xl bg-emerald-950/60 border border-emerald-500/40 text-emerald-300 text-xs font-mono flex items-center gap-2">
          <CheckCircle2 size={16} className="shrink-0" />
          <span>{uploadNotice}</span>
        </div>
      )}
    </div>
  );
};
