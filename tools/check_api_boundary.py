"""Reject dependencies from Babel's Java API back into its implementation."""
from pathlib import Path
import re

API = Path(__file__).resolve().parents[1] / 'src/main/java/com/susen36/babel/api'
# Also match fully qualified references and static/wildcard imports, not just imports.
INTERNAL = re.compile(r'\bcom\s*\.\s*susen36\s*\.\s*babel\s*\.\s*(?!\s*api\b)(?:\w+|\*)')


def main():
    violations = []
    files = sorted(API.rglob('*.java'))
    if not files:
        raise SystemExit(f'No Java API sources in {API}')
    for path in files:
        source = path.read_text(encoding='utf-8')
        if path == API / 'collectible/CollectibleRegistration.java':
            # Allow the inlined provider ID, not the entry point class or its other members.
            source = source.replace('\nimport static com.susen36.babel.BabelMod.MODID;\n', '\n\n')
        for match in INTERNAL.finditer(source):
            line = source.count('\n', 0, match.start()) + 1
            violations.append(f'{path.relative_to(API)}:{line}: {match.group()}')
    if violations:
        raise SystemExit('\n'.join(violations))
    print('Babel API boundary OK')


if __name__ == '__main__':
    main()