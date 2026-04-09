import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { CheckCircle } from 'lucide-react';
import { api } from '../utils/api';

export default function PaymentSuccess() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('checking');
  const [subscription, setSubscription] = useState(null);

  const transactionId = searchParams.get('tx_id');
  const plan = searchParams.get('plan');

  useEffect(() => {
    if (!transactionId) {
      navigate('/plans');
      return;
    }

    const checkPayment = async () => {
      try {
        const response = await api.getPaymentStatus(transactionId);

        if (response.status === 'approved') {
          setStatus('approved');
          setSubscription(response);
          setTimeout(() => navigate('/discover'), 3000);
        } else if (response.status === 'pending') {
          // Continuar verificando
          setTimeout(checkPayment, 2000);
        } else {
          setStatus('failed');
        }
      } catch (err) {
        console.error('Erro ao verificar pagamento:', err);
        setStatus('error');
      }
    };

    checkPayment();
  }, [transactionId, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center px-4 bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <div className="max-w-sm w-full">
        {status === 'checking' && (
          <div className="text-center">
            <div className="mb-4 flex justify-center">
              <div className="w-16 h-16 rounded-full bg-pink-500/20 flex items-center justify-center animate-pulse">
                <div className="w-12 h-12 rounded-full bg-pink-500/40 animate-spin" />
              </div>
            </div>
            <h1 className="text-2xl font-bold text-white mb-2">Processando Pagamento</h1>
            <p className="text-white/60 text-sm">Validando sua transação...</p>
          </div>
        )}

        {status === 'approved' && (
          <div className="text-center">
            <div className="mb-4 flex justify-center">
              <CheckCircle size={64} className="text-green-400" />
            </div>
            <h1 className="text-2xl font-bold text-white mb-2">Pagamento Aprovado! ✓</h1>
            <p className="text-white/60 text-sm mb-4">
              Seu plano {subscription?.plan} foi ativado com sucesso
            </p>
            <div className="glass rounded-xl p-4 mb-6">
              <p className="text-white/50 text-xs mb-1">Minutos disponíveis</p>
              <p className="text-2xl font-bold text-pink-400">
                {subscription?.minutes === 999999 ? '∞' : subscription?.minutes}
              </p>
              <p className="text-white/40 text-xs mt-2">Válido até {new Date(subscription?.expires_at).toLocaleDateString('pt-BR')}</p>
            </div>
            <p className="text-white/40 text-xs">Redirecionando em segundos...</p>
          </div>
        )}

        {status === 'failed' && (
          <div className="text-center">
            <div className="mb-4 flex justify-center">
              <div className="w-16 h-16 rounded-full bg-red-500/20 flex items-center justify-center">
                <div className="text-4xl">✗</div>
              </div>
            </div>
            <h1 className="text-2xl font-bold text-white mb-2">Pagamento Recusado</h1>
            <p className="text-white/60 text-sm mb-6">
              Sua transação foi recusada. Tente novamente com outro método.
            </p>
            <button
              onClick={() => navigate('/plans')}
              className="w-full py-3 gradient-primary text-white rounded-xl font-semibold"
            >
              Tentar Novamente
            </button>
          </div>
        )}

        {status === 'error' && (
          <div className="text-center">
            <div className="mb-4 flex justify-center">
              <div className="w-16 h-16 rounded-full bg-yellow-500/20 flex items-center justify-center">
                <div className="text-4xl">⚠</div>
              </div>
            </div>
            <h1 className="text-2xl font-bold text-white mb-2">Erro ao Verificar</h1>
            <p className="text-white/60 text-sm mb-6">
              Não conseguimos verificar seu pagamento. Tente novamente.
            </p>
            <button
              onClick={() => navigate('/plans')}
              className="w-full py-3 gradient-primary text-white rounded-xl font-semibold"
            >
              Voltar aos Planos
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
