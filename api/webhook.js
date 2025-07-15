import admin from "firebase-admin";

if (!admin.apps.length) {
  admin.initializeApp({
    credential: admin.credential.cert({
      projectId: process.env.FIREBASE_PROJECT_ID,
      clientEmail: process.env.FIREBASE_CLIENT_EMAIL,
      privateKey: process.env.FIREBASE_PRIVATE_KEY.replace(/\\n/g, '\n'),
    }),
  });
}

const db = admin.firestore();

export default async function handler(req, res) {
  if (req.method !== "POST") {
    return res.status(405).json({ message: "Method Not Allowed" });
  }

  const event = req.body;
  const metadata = event?.data?.metadata;

  if (event?.event === "charge.success" && metadata?.userId) {
    const userRef = db.collection("users").doc(metadata.userId);
    const userDoc = await userRef.get();
    const oldBalance = userDoc.exists ? userDoc.data().balance || 0 : 0;

    await userRef.set(
      { balance: oldBalance + event.data.amount / 100 },
      { merge: true }
    );

    return res.status(200).json({ updated: true });
  }

  return res.status(200).json({ received: true });
}
