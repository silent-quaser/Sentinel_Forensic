import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { UploadCloud, XCircle } from 'lucide-react';
import { investigationApi, logApi, threatApi } from '../api/client';
import { Investigation, LogEntry, TimelineEvent, Threat, InvestigationNote } from '../types';
import { RawLogModal } from '../components/common/RawLogModal';
import { ThreatDetailDrawer } from '../components/common/ThreatDetailDrawer';
import { AddNoteModal } from '../components/common/AddNoteModal';
import { InvestigationHeader } from '../components/investigation/InvestigationHeader';
import { InvestigationOverviewTab } from '../components/investigation/InvestigationOverviewTab';
import { InvestigationTimelineTab } from '../components/investigation/InvestigationTimelineTab';
import { InvestigationEventsTab } from '../components/investigation/InvestigationEventsTab';
import { InvestigationThreatsTab } from '../components/investigation/InvestigationThreatsTab';
import { InvestigationNotesTab } from '../components/investigation/InvestigationNotesTab';
import { InvestigationReportTab } from '../components/investigation/InvestigationReportTab';

export default function InvestigationDetail() {
  const { id } = useParams<{ id: string }>();
  const investigationId = Number(id);

  const [investigation, setInvestigation] = useState<Investigation | null>(null);
  const [activeTab, setActiveTab] = useState<'overview' | 'timeline' | 'events' | 'threats' | 'notes' | 'report'>('overview');
  
  const [timeline, setTimeline] = useState<TimelineEvent[]>([]);
  const [threats, setThreats] = useState<Threat[]>([]);
  const [notes, setNotes] = useState<InvestigationNote[]>([]);
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [logPage, setLogPage] = useState(0);
  const [totalLogPages, setTotalLogPages] = useState(1);
  const [totalLogElements, setTotalLogElements] = useState(0);

  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedEventType, setSelectedEventType] = useState('');
  const [selectedUsername, setSelectedUsername] = useState('');

  const [selectedLogForModal, setSelectedLogForModal] = useState<LogEntry | null>(null);
  const [selectedThreatForDrawer, setSelectedThreatForDrawer] = useState<Threat | null>(null);
  const [isAddNoteOpen, setIsAddNoteOpen] = useState(false);
  const [isUploadModalOpen, setIsUploadModalOpen] = useState(false);

  const [runningDetection, setRunningDetection] = useState(false);
  const [detectionNotice, setDetectionNotice] = useState<string | null>(null);
  const [uploadFile, setUploadFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const [uploadNotice, setUploadNotice] = useState<string | null>(null);
  const [downloadingPdf, setDownloadingPdf] = useState(false);

  const loadInvestigation = async () => {
    try {
      const data = await investigationApi.getById(investigationId);
      setInvestigation(data);
    } catch (err) {
      console.error('Failed to load investigation', err);
    }
  };

  const loadTimeline = async () => {
    try {
      const data = await logApi.getTimeline(investigationId);
      setTimeline(data);
    } catch (err) {
      console.error('Failed to load timeline', err);
    }
  };

  const loadThreats = async () => {
    try {
      const data = await threatApi.getInvestigationThreats(investigationId);
      setThreats(data);
    } catch (err) {
      console.error('Failed to load threats', err);
    }
  };

  const loadNotes = async () => {
    try {
      const data = await investigationApi.getNotes(investigationId);
      setNotes(data);
    } catch (err) {
      console.error('Failed to load notes', err);
    }
  };

  const loadLogs = async () => {
    try {
      const data = await logApi.getInvestigationLogs(investigationId, {
        keyword: searchKeyword || undefined,
        eventType: selectedEventType || undefined,
        username: selectedUsername || undefined,
        page: logPage,
        size: 25,
      });
      setLogs(data.content);
      setTotalLogPages(data.totalPages);
      setTotalLogElements(data.totalElements);
    } catch (err) {
      console.error('Failed to load logs', err);
    }
  };

  useEffect(() => {
    if (investigationId) {
      loadInvestigation();
      loadTimeline();
      loadThreats();
      loadNotes();
      loadLogs();
    }
  }, [investigationId]);

  useEffect(() => {
    loadLogs();
  }, [logPage, searchKeyword, selectedEventType, selectedUsername]);

  const handleRunDetection = async () => {
    setRunningDetection(true);
    setDetectionNotice(null);
    try {
      const result = await threatApi.runDetection(investigationId);
      setDetectionNotice(
        `Detection complete: ${result.newThreatsDetected} new threat(s) detected, ${result.existingThreatsSkipped} duplicate(s) suppressed.`
      );
      loadThreats();
      loadTimeline();
      loadInvestigation();
    } catch (err: any) {
      setDetectionNotice(err?.response?.data?.message || 'Threat detection failed');
    } finally {
      setRunningDetection(false);
    }
  };

  const handleUploadAdditionalLogs = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!uploadFile) return;

    setUploading(true);
    setUploadNotice(null);
    try {
      const res = await logApi.upload(investigationId, uploadFile);
      setUploadNotice(
        `Log ingested: ${res.parsedCount} events parsed, ${res.skippedLines} skipped.`
      );
      setUploadFile(null);
      setIsUploadModalOpen(false);
      loadInvestigation();
      loadTimeline();
      loadLogs();
    } catch (err: any) {
      setUploadNotice(err?.response?.data?.message || 'Upload failed');
    } finally {
      setUploading(false);
    }
  };

  const handleDownloadPdf = async () => {
    setDownloadingPdf(true);
    try {
      await investigationApi.downloadPdfReport(
        investigationId,
        `SentinelForensic_Report_${investigation?.investigationId || investigationId}.pdf`
      );
    } catch (err) {
      alert('Failed to generate forensic PDF report');
    } finally {
      setDownloadingPdf(false);
    }
  };

  const handleStatusChange = async (newStatus: string) => {
    try {
      const updated = await investigationApi.updateStatus(investigationId, newStatus);
      setInvestigation(updated);
    } catch (err) {
      alert('Failed to update case status');
    }
  };

  if (!investigation) {
    return (
      <div className="p-12 text-center text-xs text-slate-400 font-mono">
        Loading forensic case dossier #{investigationId}...
      </div>
    );
  }

  return (
    <div className="space-y-6 pb-16">
      <InvestigationHeader
        investigation={investigation}
        timelineCount={timeline.length}
        threatCount={threats.length}
        runningDetection={runningDetection}
        downloadingPdf={downloadingPdf}
        detectionNotice={detectionNotice}
        uploadNotice={uploadNotice}
        onRunDetection={handleRunDetection}
        onOpenUpload={() => setIsUploadModalOpen(true)}
        onDownloadPdf={handleDownloadPdf}
        onOpenAddNote={() => setIsAddNoteOpen(true)}
        onStatusChange={handleStatusChange}
      />

      <div className="flex items-center gap-2 border-b border-slate-800 pb-1 overflow-x-auto select-none">
        {[
          { key: 'overview', label: 'Case Overview', count: null },
          { key: 'timeline', label: 'Forensic Timeline', count: timeline.length },
          { key: 'events', label: 'Event Explorer', count: totalLogElements },
          { key: 'threats', label: 'Threats & Detections', count: threats.length },
          { key: 'notes', label: 'Case Notes', count: notes.length },
          { key: 'report', label: 'Report Preview', count: null },
        ].map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key as any)}
            className={`px-4 py-2.5 rounded-xl text-xs font-semibold transition-all cursor-pointer whitespace-nowrap flex items-center gap-2 ${
              activeTab === tab.key
                ? 'bg-cyan-500/10 text-cyan-400 border border-cyan-500/30 shadow-[0_0_12px_rgba(6,182,212,0.15)]'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-900 border border-transparent'
            }`}
          >
            <span>{tab.label}</span>
            {tab.count !== null && (
              <span className={`px-1.5 py-0.2 rounded-full text-[10px] font-mono ${
                activeTab === tab.key ? 'bg-cyan-950 text-cyan-300 border border-cyan-500/40' : 'bg-slate-800 text-slate-400'
              }`}>
                {tab.count}
              </span>
            )}
          </button>
        ))}
      </div>

      {activeTab === 'overview' && (
        <InvestigationOverviewTab
          investigation={investigation}
          threats={threats}
          timeline={timeline}
          onSelectThreat={(t) => setSelectedThreatForDrawer(t)}
          onRunDetection={handleRunDetection}
        />
      )}

      {activeTab === 'timeline' && (
        <InvestigationTimelineTab
          timeline={timeline}
          onSelectLog={(log) => setSelectedLogForModal(log)}
        />
      )}

      {activeTab === 'events' && (
        <InvestigationEventsTab
          logs={logs}
          totalLogElements={totalLogElements}
          totalLogPages={totalLogPages}
          logPage={logPage}
          searchKeyword={searchKeyword}
          selectedEventType={selectedEventType}
          selectedUsername={selectedUsername}
          onSearchChange={(k) => { setSearchKeyword(k); setLogPage(0); }}
          onEventTypeChange={(t) => { setSelectedEventType(t); setLogPage(0); }}
          onUsernameChange={(u) => { setSelectedUsername(u); setLogPage(0); }}
          onPageChange={(p) => setLogPage(p)}
          onSelectLog={(log) => setSelectedLogForModal(log)}
        />
      )}

      {activeTab === 'threats' && (
        <InvestigationThreatsTab
          threats={threats}
          runningDetection={runningDetection}
          onRunDetection={handleRunDetection}
          onSelectThreat={(t) => setSelectedThreatForDrawer(t)}
        />
      )}

      {activeTab === 'notes' && (
        <InvestigationNotesTab
          notes={notes}
          onOpenAddNote={() => setIsAddNoteOpen(true)}
        />
      )}

      {activeTab === 'report' && (
        <InvestigationReportTab
          investigation={investigation}
          threats={threats}
          timeline={timeline}
          downloadingPdf={downloadingPdf}
          onDownloadPdf={handleDownloadPdf}
        />
      )}

      {selectedLogForModal && (
        <RawLogModal log={selectedLogForModal} onClose={() => setSelectedLogForModal(null)} />
      )}

      {selectedThreatForDrawer && (
        <ThreatDetailDrawer
          threat={selectedThreatForDrawer}
          onClose={() => setSelectedThreatForDrawer(null)}
          onStatusUpdated={(updated) => {
            setSelectedThreatForDrawer(updated);
            loadThreats();
          }}
        />
      )}

      <AddNoteModal
        investigationId={investigationId}
        isOpen={isAddNoteOpen}
        onClose={() => setIsAddNoteOpen(false)}
        onNoteAdded={(newNote) => setNotes((prev) => [newNote, ...prev])}
      />

      {isUploadModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-2xl w-full max-w-lg overflow-hidden shadow-2xl p-6 space-y-5 animate-in fade-in zoom-in duration-150">
            <div className="flex items-center justify-between border-b border-slate-800 pb-3">
              <div className="flex items-center gap-2 text-cyan-400 font-mono text-sm font-semibold">
                <UploadCloud size={18} />
                <span>UPLOAD FORENSIC LOG FILE</span>
              </div>
              <button onClick={() => setIsUploadModalOpen(false)} className="text-slate-400 hover:text-white p-1">
                <XCircle size={18} />
              </button>
            </div>

            <form onSubmit={handleUploadAdditionalLogs} className="space-y-4">
              <div
                className="border-2 border-dashed border-slate-700 hover:border-cyan-400 rounded-xl p-6 text-center cursor-pointer bg-slate-950/60"
                onClick={() => document.getElementById('modal-file-input')?.click()}
              >
                <input
                  id="modal-file-input"
                  type="file"
                  accept=".log,.txt"
                  className="hidden"
                  onChange={(e) => setUploadFile(e.target.files?.[0] || null)}
                />
                <UploadCloud size={32} className="mx-auto text-cyan-400 mb-2" />
                {uploadFile ? (
                  <div>
                    <p className="text-xs font-mono font-bold text-white">{uploadFile.name}</p>
                    <p className="text-[11px] font-mono text-cyan-400 mt-1">{(uploadFile.size / 1024).toFixed(1)} KB</p>
                  </div>
                ) : (
                  <div>
                    <p className="text-xs font-medium text-slate-300">Select log file (.log, .txt)</p>
                    <p className="text-[11px] text-slate-500 mt-1">Files will be parsed into forensic timeline entries</p>
                  </div>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setIsUploadModalOpen(false)}
                  className="px-4 py-2 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={uploading || !uploadFile}
                  className="px-5 py-2 text-xs font-semibold bg-cyan-600 hover:bg-cyan-500 text-white rounded-xl disabled:opacity-50"
                >
                  {uploading ? 'Processing & Ingesting...' : 'Upload & Parse'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
