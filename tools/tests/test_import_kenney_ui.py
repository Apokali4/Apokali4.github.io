import tempfile
import unittest
import zipfile
from pathlib import Path

from import_kenney_ui import destination_for, import_pack, resource_name, safe_archive_path


class KenneyImportTests(unittest.TestCase):
    def test_resource_names_are_android_safe_and_stable(self):
        value = resource_name("PNG/Buttons/Blue/Button Large.png")
        self.assertEqual(value, "kenney_png_buttons_blue_button_large")
        self.assertRegex(value, r"^[a-z][a-z0-9_]*$")

    def test_archive_path_rejects_traversal(self):
        self.assertIsNone(safe_archive_path("../outside.png"))
        self.assertIsNone(safe_archive_path("/root.png"))
        self.assertEqual(safe_archive_path("PNG/button.png").as_posix(), "PNG/button.png")

    def test_import_places_supported_files_in_android_resource_types(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            archive = root / "pack.zip"
            with zipfile.ZipFile(archive, "w") as bundle:
                bundle.writestr("PNG/Blue/Button.png", b"png-data")
                bundle.writestr("Fonts/Interface.ttf", b"font-data")
                bundle.writestr("Sounds/Click.wav", b"audio-data")
                bundle.writestr("Vector/Button.svg", b"<svg/>")
                bundle.writestr("../escape.png", b"bad")
            manifest = import_pack(archive, root)
            paths = {item["installed_path"] for item in manifest["files"]}
            self.assertIn("android/app/src/main/res/drawable-nodpi/kenney_png_blue_button.png", paths)
            self.assertIn("android/app/src/main/res/font/kenney_fonts_interface.ttf", paths)
            self.assertIn("android/app/src/main/res/raw/kenney_sounds_click.wav", paths)
            self.assertFalse(any("escape" in path for path in paths))
            self.assertTrue((root / "android/assets/kenney/kenney_ui-pack.zip").exists())


if __name__ == "__main__":
    unittest.main()
