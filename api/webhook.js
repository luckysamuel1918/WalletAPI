export const config = {
  api: {
    bodyParser: true,
  },
};

import admin from "firebase-admin";

try {
  if (!admin.apps.length) {
    console.log("Initializing Firebase Admin...");
    admin.initializeApp({
      credential: admin.credential.cert({
        projectId: process.env.FIREBASE_PROJECT_ID,
        clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
        privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
      }),
    });
  }
} catch (err) {
  console.error("🔥 Firebase Admin Init Failed:", err.message);
}

const db = admin.firestore();

export default async function handler(req, res) {
  console.log("Webhook function triggered");

  if (req.method !== "POST") {
    console.log("Wrong method:", req.method);
    return res.status(405).json({ message: "Method Not Allowed" });
  }

  try {
    const event = req.body;
    console.log("Received Event:", JSON.stringify(event));

    const metadata = event?.data?.metadata;

    if (event?.event === "charge.success" && metadata?.userId) {
      const userRef = db.collection("users").doc(metadata.userId);
      const userDoc = await userRef.get();
      const oldBalance = userDoc.exists ? userDoc.data().balance || 0 : 0;
      const newBalance = oldBalance + event.data.amount / 100;

      console.log(`Old Balance: ${oldBalance} → New Balance: ${newBalance}`);

      await userRef.set(
        { balance: newBalance },
        { merge: true }
      );

      return res.status(200).json({ updated: true });
    }

    return res.status(200).json({ received: true });
  } catch (err) {
    console.error("🔥 Webhook Handler Error:", err.message);
    return res.status(500).json({ error: "Internal Server Error", details: err.message });
  }
}
