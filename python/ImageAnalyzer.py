import numpy as np
import matplotlib.pyplot as plt
import os
import sys

from PIL import Image

class ImageAnalyzer:

    def __init__(self):
        self.results_dir = "results/metrics"
        os.makedirs(self.results_dir, exist_ok=True)

    def main(self):
        self.analyze(sys.argv[1], sys.argv[2], sys.argv[3])


    def analyze(self, original: str, encrypted: str, encrypted_another_key: str):
        original_image = np.array(Image.open("imgs/" + original).convert('RGB'))
        encrypted = np.array(Image.open("imgs/encrypted/" + encrypted).convert('RGB'))
        encrypted_another_key = np.array(Image.open("imgs/encrypted/" + encrypted_another_key).convert('RGB'))

        self.plot_channel_histograms(self, original, original_image, encrypted)

        original_entropy = self.channel_entropy(original_image)
        encrypted_entropy = self.channel_entropy(encrypted)

        orig_correlation = {}
        enc_correlation = {}
        for mode in ['H', 'V', 'D']:
            orig_correlation[mode] = self.calc_correlation(self, original_image, mode)
            enc_correlation[mode] = self.calc_correlation(self, encrypted, mode)

        npcr_orig_encr = self.calc_npcr(original_image, encrypted)
        npcr_encr_encr = self.calc_npcr(encrypted, encrypted_another_key)

        uaci_orig_encr = self.calc_uaci(original_image, encrypted)
        uaci_encr_encr = self.calc_uaci(encrypted, encrypted_another_key)

        avalanche = self.calc_avalanche(encrypted, encrypted_another_key)

        self.save_results(original, original_entropy, encrypted_entropy, orig_correlation, enc_correlation,
                          npcr_orig_encr, npcr_encr_encr, uaci_orig_encr, uaci_encr_encr, avalanche)


    @staticmethod
    def plot_channel_histograms(self, name, original, encrypted, title1='Original', title2='Encrypted', filename='histogram.png'):
        plt.figure(figsize=(12, 6))
        for i, color in enumerate(['red', 'green', 'blue']):
            plt.subplot(2, 3, i+1)
            plt.hist(original[:,:,i].flatten(), bins=256, color=color, alpha=0.7)
            plt.title(f'{title1} {color}')
            plt.subplot(2, 3, i+4)
            plt.hist(encrypted[:,:,i].flatten(), bins=256, color=color, alpha=0.7)
            plt.title(f'{title2} {color}')
        plt.tight_layout()
        plt.savefig(os.path.join(self.results_dir, name[:-4] + "_" + filename))
        plt.close()


    @staticmethod
    def channel_entropy(image: np.ndarray):
        ent = []
        for channel in range(3):
            vals, counts = np.unique(image[:, :, channel], return_counts=True)
            p = counts / counts.sum()
            ent.append(-np.sum(p * np.log2(p)))
        return ent


    @staticmethod
    def calc_correlation(self, image, mode: str):
        h, w, _ = image.shape
        results = []
        for channel in range(3):
            if mode == 'H':
                x, y = image[:, :-1, channel].flatten(), image[:, 1:, channel].flatten()
            elif mode == 'V':
                x, y = image[:-1, :, channel].flatten(), image[1:, :, channel].flatten()
            elif mode == 'D':
                x, y = image[:-1, :-1, channel].flatten(), image[1:, 1:, channel].flatten()
            else:
                raise ValueError()
            results.append(self.compute_correlation(x, y))

        return results


    @staticmethod
    def compute_correlation(x, y):
        x = np.asarray(x, dtype=np.float64).flatten()
        y = np.asarray(y, dtype=np.float64).flatten()

        mean_x = np.mean(x)
        mean_y = np.mean(y)

        numerator = np.sum((x - mean_x) * (y - mean_y))
        denominator = np.sqrt(np.sum((x - mean_x) ** 2) * np.sum((y - mean_y) ** 2))

        #numerator = np.sum(x * y) - (np.sum(x) * np.sum(y))
        #denominator = np.sqrt((np.sum(x ** 2) - np.sum(x) ** 2) * (np.sum(y ** 2) - np.sum(y) ** 2))

        if denominator == 0:
            return 0.0

        return numerator / denominator


    @staticmethod
    def calc_npcr(img1, img2):
        diff = np.sum(np.any(img1 != img2, axis=2))
        total = img1.shape[0] * img1.shape[1]

        return (diff / total) * 100.0


    @staticmethod
    def calc_uaci(img1, img2):
        img1 = img1.astype(np.float64)
        img2 = img2.astype(np.float64)

        total = img1.size

        sum_diff = np.sum(np.abs(img1 - img2) / (total * 255.0))

        return sum_diff * 100.0


    @staticmethod
    def calc_avalanche(enc1, enc2):
        bits1 = np.unpackbits(np.frombuffer(enc1.tobytes(), dtype=np.uint8))
        bits2 = np.unpackbits(np.frombuffer(enc2.tobytes(), dtype=np.uint8))

        different_bits = np.sum(bits1 != bits2)
        total_bits = len(bits1)

        return (different_bits / total_bits) * 100


    @staticmethod
    def save_results(original, original_entropy, encrypted_entropy, orig_correlation, enc_correlation,
                          npcr_orig_encr, npcr_encr_encr, uaci_orig_encr, uaci_encr_encr, avalanche):
        report = []

        report.append(f'Original Entropy: {original_entropy}')
        report.append(f'Encrypted Entropy: {encrypted_entropy}')

        report.append(f'H-Correlation (before): {orig_correlation['H']}')
        report.append(f'V-Correlation (before): {orig_correlation['V']}')
        report.append(f'D-Correlation (before): {orig_correlation['D']}')

        report.append(f'H-Correlation (after): {enc_correlation['H']}')
        report.append(f'V-Correlation (after): {enc_correlation['V']}')
        report.append(f'D-Correlation (after): {enc_correlation['D']}')

        report.append(f'NPCR Original-Encrypted: {npcr_orig_encr}')
        report.append(f'NPCR One Bit Different Key: {npcr_encr_encr}')

        report.append(f'UACI Original-Encrypted: {uaci_orig_encr}')
        report.append(f'UACI One Bit Different Key: {uaci_encr_encr}')

        report.append(f'Avalanche: {avalanche}')

        with open(f'results/metrics/{original[:-4]}_results.txt', 'w', encoding='utf-8') as f:
            f.write('\n'.join(report))


if __name__ == '__main__':
    analyzer = ImageAnalyzer()
    analyzer.analyze(sys.argv[1], sys.argv[2], sys.argv[3])