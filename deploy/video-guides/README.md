# Tutoriais em português

Estes três vídeos são originais do LoveAll: ilustrações, texto em português e indicadores animados de progresso. Não têm áudio, intérprete, avatar ou tradução em Libras. Essa limitação aparece na própria imagem e na tela do aplicativo. A produção desse formato foi escolhida pelo responsável; os vídeos em Libras continuam pendentes.

| Arquivo no Android (`app/src/main/res/raw`) | Duração | Assunto |
| --- | --- | --- |
| `tutorial_discovery.mp4` | 46 s | Descobrir, curtir, match e conversa |
| `tutorial_safety.mp4` | 49 s | Denunciar, bloquear, desfazer match e excluir conta |
| `tutorial_pcd.mp4` | 50 s | Participação voluntária, consentimento e saída |

Os arquivos somam 887.134 bytes, usam H.264 Baseline 3.1, 720 × 1280, 20 fps e YUV420p. São incluídos no APK/AAB e a reprodução não faz requisições de rede. O restante do aplicativo mantém seus requisitos normais de autenticação e conexão com a API. Os arquivos SRT acompanham os roteiros para edição; o texto já faz parte das imagens dos vídeos. A transcrição também é um recurso Android com suporte à escala de fonte e ao leitor de tela.

## Como atualizar

1. Edite `roteiros.json`, mantendo títulos curtos e instruções coerentes com as telas reais. Não inclua contas, conversas, fotos ou dados pessoais de usuários.
2. Em Windows, instale Pillow e disponibilize um executável FFmpeg com libx264. A geração utiliza a fonte Segoe UI instalada no Windows. Essas ferramentas são apenas de produção dos arquivos, sem dependências adicionais no Android.
3. Execute `python scripts/generate_tutorial_videos.py --ffmpeg CAMINHO_DO_FFMPEG.exe` a partir do repositório. O script atualiza MP4, SRT, transcrições Android e quadros para revisão. Gere os vídeos antes de iniciar um build Android.
4. Confira todos os textos nos storyboards e nos quadros extraídos dos vídeos, valide o tempo de leitura e teste reprodução, troca de assunto, pausa ao sair da Activity e leitura da transcrição.

`TutorialVideoTest` verifica os três recursos locais com MediaMetadataRetriever, decodifica quadros, confere formato/duração, reproduz cada assunto pelo player real e verifica pausa em segundo plano. A verificação externa com FFmpeg decodificou todos os quadros dos três arquivos. Evidências ficam em `qa/video-guides`.

## Libras

O catálogo remoto de Libras continua separado em `res/values/libras_videos.xml`, sem endereços preenchidos. Não apontar esse catálogo para os tutoriais em português. Para preencher, produzir conteúdo sinalizado autorizado e revisado por pessoa fluente, com legendas em português e validação no Android.

Na consulta de 21/11/2026, o [VLibras Vídeo](https://video.vlibras.gov.br/) informava restrição ao uso de suas traduções automáticas em produções audiovisuais e conteúdos como aulas. Por isso, esse serviço não foi utilizado para gerar os tutoriais do aplicativo.
