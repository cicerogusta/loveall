"""Gera tutoriais originais em português. Não gera nem simula sinais de Libras.

Requer Pillow e ffmpeg. Recebe caminho explícito do encoder, sem instalar nada
nas dependências do aplicativo. Os roteiros e tempos ficam no JSON versionado.
"""

import argparse
import hashlib
import json
import math
from pathlib import Path
import subprocess
import tempfile
import xml.etree.ElementTree as ET

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
BG = '#FBF5F8'
INK = '#302332'
PRIMARY = '#99385E'
MUTED = '#695A68'
PALE = '#F2DCE6'
WHITE = '#FFFFFF'


def font(size, bold=False):
    name = 'segoeuib.ttf' if bold else 'segoeui.ttf'
    return ImageFont.truetype(str(Path('C:/Windows/Fonts') / name), size)


def wrapped(draw, text, face, width):
    lines, current = [], ''
    for word in text.split():
        candidate = f'{current} {word}'.strip()
        if current and draw.textlength(candidate, font=face) > width:
            lines.append(current)
            current = word
        else:
            current = candidate
    if current:
        lines.append(current)
    return lines


def text_block(draw, text, xy, size, width, color=INK, bold=False, max_y=None):
    face = font(size, bold)
    x, y = xy
    for line in wrapped(draw, text, face, width):
        if max_y is not None and y + size > max_y:
            raise ValueError(f'Texto excede a área: {text}')
        draw.text((x, y), line, font=face, fill=color)
        y += size + 9
    return y


def pill(draw, box, text, fill=PRIMARY, color=WHITE, size=25):
    draw.rounded_rectangle(box, radius=22, fill=fill)
    face = font(size, True)
    x0, y0, x1, y1 = box
    draw.text(((x0 + x1 - draw.textlength(text, font=face)) / 2, (y0 + y1 - size) / 2 - 4), text, font=face, fill=color)


def illustration(draw, visual):
    draw.rounded_rectangle((155, 510, 565, 1008), radius=38, fill=WHITE, outline='#E6D4DE', width=3)
    draw.rounded_rectangle((308, 526, 412, 534), radius=4, fill='#D8C3CF')
    label = {
        'discover': 'Descobrir', 'like': 'Seu próximo encontro', 'match': 'É um match!',
        'matches': 'Matches', 'chat': 'Conversa', 'safety': 'Sua segurança',
        'report': 'Denunciar', 'block': 'Bloquear', 'unmatch': 'Desfazer match',
        'delete': 'Seu perfil', 'confirm_delete': 'Excluir conta', 'pcd': 'Comunidade PcD',
        'pcd_info': 'Participação voluntária', 'consent': 'Seu consentimento', 'leave': 'Sair da comunidade',
    }[visual]
    text_block(draw, label, (187, 563), 27, 348, bold=True, max_y=646)
    if visual in ['discover', 'like', 'match', 'matches']:
        draw.rounded_rectangle((188, 655, 532, 872), radius=24, fill=PALE)
        draw.ellipse((310, 682, 410, 782), fill=PRIMARY)
        draw.rounded_rectangle((270, 789, 450, 854), radius=32, fill=PRIMARY)
        draw.text((234, 882), 'Perfil ilustrativo', font=font(25), fill=MUTED)
        pill(draw, (188, 930, 347, 982), 'Passar', PALE, PRIMARY, 23)
        pill(draw, (365, 930, 532, 982), 'Curtir', size=23)
    elif visual == 'chat':
        pill(draw, (185, 674, 440, 733), 'Olá, tudo bem?', PALE, INK, 23)
        pill(draw, (280, 772, 535, 831), 'Vamos conversar?', size=23)
        draw.rounded_rectangle((185, 921, 535, 981), radius=22, outline=PRIMARY, width=2)
        draw.text((201, 935), 'Sua mensagem...', font=font(25), fill=MUTED)
    elif visual in ['pcd', 'pcd_info', 'consent', 'leave']:
        text_block(draw, 'A escolha é sua.\nSem laudo ou diagnóstico.', (190, 665), 28, 335, max_y=830)
        draw.rounded_rectangle((190, 836, 225, 871), radius=6, outline=PRIMARY, width=3)
        if visual == 'consent':
            draw.line((195, 853, 204, 863, 220, 842), fill=PRIMARY, width=5)
        text_block(draw, 'Li e concordo', (239, 837), 26, 290)
        pill(draw, (188, 929, 532, 984), 'Sair da comunidade' if visual == 'leave' else 'Participação opcional', size=23)
    else:
        options = ['Denunciar', 'Bloquear', 'Desfazer match']
        if visual in ['delete', 'confirm_delete']:
            options = ['Perfil', 'Senha LoveAll', 'Excluir conta']
        for index, option in enumerate(options):
            active = option.lower().split()[0] in label.lower()
            pill(draw, (188, 672 + index * 95, 532, 737 + index * 95), option, PRIMARY if active else PALE, WHITE if active else INK, 25)


def scene_image(guide, scene, index, total):
    image = Image.new('RGB', (720, 1280), BG)
    draw = ImageDraw.Draw(image)
    draw.text((48, 42), 'LOVEALL', font=font(28, True), fill=PRIMARY)
    draw.text((530, 48), f'{index + 1:02d} / {total:02d}', font=font(23, True), fill=MUTED)
    text_block(draw, scene['title'], (48, 111), 48, 624, bold=True, max_y=240)
    text_block(draw, scene['text'], (48, 262), 32, 624, max_y=490)
    illustration(draw, scene['visual'])
    text_block(draw, scene['action'], (48, 1052), 33, 624, color=PRIMARY, bold=True, max_y=1175)
    draw.text((48, 1190), 'TUTORIAL EM PORTUGUÊS · SEM LIBRAS', font=font(22, True), fill=MUTED)
    return image


def timestamp(seconds):
    millis = round(seconds * 1000)
    return f'{millis // 3600000:02d}:{millis // 60000 % 60:02d}:{millis // 1000 % 60:02d},{millis % 1000:03d}'


def render(encoder, config, guide, output, previews):
    target = output / f'{guide["id"]}.mp4'
    captions, elapsed = [], 0
    command = [str(encoder), '-hide_banner', '-loglevel', 'error', '-y', '-f', 'rawvideo', '-vcodec', 'rawvideo',
               '-pix_fmt', 'rgb24', '-s', '720x1280', '-r', str(config['fps']), '-i', '-', '-an',
               '-c:v', 'libx264', '-preset', 'fast', '-crf', '25', '-profile:v', 'baseline', '-level:v', '3.1',
               '-pix_fmt', 'yuv420p', '-movflags', '+faststart', str(target)]
    with tempfile.TemporaryFile() as error_log:
        process = subprocess.Popen(command, stdin=subprocess.PIPE, stderr=error_log)
        try:
            for index, scene in enumerate(guide['scenes']):
                base = scene_image(guide, scene, index, len(guide['scenes']))
                base.save(previews / f'{guide["id"]}_{index + 1:02d}.png')
                frames = round(scene['seconds'] * config['fps'])
                for frame in range(frames):
                    image = base.copy()
                    draw = ImageDraw.Draw(image)
                    progress = (index + frame / frames) / len(guide['scenes'])
                    draw.rounded_rectangle((48, 1242, 672, 1251), radius=4, fill=PALE)
                    draw.rounded_rectangle((48, 1242, 48 + max(8, progress * 624), 1251), radius=4, fill=PRIMARY)
                    pulse = round(6 + 4 * math.sin(frame / config['fps'] * math.pi))
                    draw.ellipse((656 - pulse, 60 - pulse, 656 + pulse, 60 + pulse), fill=PRIMARY)
                    process.stdin.write(image.tobytes())
                captions.append(f'{index + 1}\n{timestamp(elapsed)} --> {timestamp(elapsed + scene["seconds"])}\n{scene["title"]}\n{scene["text"]}\n')
                elapsed += scene['seconds']
            process.stdin.close()
            if process.wait() != 0:
                error_log.seek(0)
                raise RuntimeError(error_log.read().decode(errors='replace'))
        finally:
            if process.poll() is None:
                process.kill()
                process.wait()
    (ROOT / 'deploy/video-guides' / f'{guide["id"]}.srt').write_text('\n'.join(captions), encoding='utf-8')
    return {'id': guide['id'], 'seconds': elapsed, 'bytes': target.stat().st_size,
            'sha256': hashlib.sha256(target.read_bytes()).hexdigest(), 'libras': False}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--ffmpeg', required=True, type=Path)
    args = parser.parse_args()
    config = json.loads((ROOT / 'deploy/video-guides/roteiros.json').read_text(encoding='utf-8'))
    transcripts = ET.Element('resources')
    for guide in config['guides']:
        node = ET.SubElement(transcripts, 'string', name=guide['id'] + '_transcript')
        node.text = '\\n\\n'.join(scene['title'] + '. ' + scene['text'] for scene in guide['scenes'])
    ET.indent(transcripts, space='    ')
    ET.ElementTree(transcripts).write(ROOT / 'app/src/main/res/values/tutorial_transcripts.xml', encoding='utf-8', xml_declaration=True)
    output = ROOT / 'app/src/main/res/raw'
    previews = ROOT / 'qa/video-guides/storyboards'
    output.mkdir(parents=True, exist_ok=True)
    previews.mkdir(parents=True, exist_ok=True)
    result = []
    for guide in config['guides']:
        result.append(render(args.ffmpeg, config, guide, output, previews))
        print(f'Gerado: {guide["id"]}', flush=True)
    (ROOT / 'qa/video-guides/generation.json').write_text(json.dumps(result, indent=2) + '\n', encoding='utf-8')


if __name__ == '__main__':
    main()
