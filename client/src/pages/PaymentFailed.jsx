import { useSearchParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, AlertCircle } from 'lucide-react';

export default function PaymentFailed() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const transactionId = searchParams.get('tx_id');

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <div className="max-w-sm w-full">
        <div className="text-center">
          <div className="mb-6 flex justify-center">
            <AlertCircle size={64} className="text-red-400" />
          </div>

          <h1 className="text-3xl font-bold text-white mb-2">Pagamento Recusado</h1>

          <p className="text-white/60 text-sm mb-8">
            Infelizmente, sua transação foi recusada pelo banco ou operadora de cartão.
          </p>

          <div className="glass rounded-xl p-4 mb-6 text-left">
            <p className="text-white/50 text-xs mb-2">Código da transação</p>
            <p className="text-white font-mono text-sm break-all">{transactionId || 'N/A'}</p>
          </div>

          <div className="space-y-3 mb-6">
            <p className="text-white/40 text-xs text-left">
              <strong>Motivos comuns:</strong>
              <ul className="list-disc list-inside mt-2 space-y-1">
                <li>Cartão vencido ou desativado</li>
                <li>Limite insuficiente</li>
                <li>Transação bloqueada pelo banco</li>
                <li>Dados incorretos informados</li>
              </ul>
            </p>
          </div>

          <button
            onClick={() => navigate('/plans')}
            className="w-full py-3 gradient-primary text-white rounded-xl font-semibold mb-3 transition-all active:scale-95"
          >
            Tentar Outro Método
          </button>

          <button
            onClick={() => navigate('/discover')}
            className="w-full py-3 glass text-white rounded-xl font-semibold flex items-center justify-center gap-2"
          >
            <ArrowLeft size={18} />
            Voltar ao App
          </button>

          <p className="text-white/30 text-xs mt-6">
            Precisa de ajuda? Entre em contato com nosso suporte.
          </p>
        </div>
      </div>
    </div>
  );
}
