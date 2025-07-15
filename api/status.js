export default function handler(req, res) {
  res.status(200).json({ status: "OK", message: "Wallet API is alive 🔥" });
}
