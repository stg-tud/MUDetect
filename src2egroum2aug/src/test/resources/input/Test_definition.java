package input;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;

class Test_definition extends BufferedIndexOutput {

    public byte[] getByteCode() {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);
        try {
            for (InstructionHandle ih = start; ih != null; ih = ih.getNext()) {
                Instruction i = ih.getInstruction();
                i.dump(out); // Traverse list
            }
        } catch (IOException e) {
            System.err.println(e);
            return new byte[0];
        }
        return b.toByteArray();
    }

    public static KeyNonceBundle serverSideAuth(InputStream is, OutputStream os, PrivateKey serverPrivK) {
        /*Initialize a cipher to accept the incoming message first message*/
        Cipher c = null;
        try {
            c = Cipher.getInstance("RSA");
            c.init(Cipher.DECRYPT_MODE, serverPrivK);
        }
        catch (Exception e) {
            System.out.println("Server's private key was not initialized correctly.");
            e.printStackTrace();
            System.exit( 1 );
        }

        /*Receive the first message*/
        /*Because RSA is DUMB WITH CIPHER STREAMS, doing it old school*/
        BufferedInputStream bis = new BufferedInputStream(is);
        byte[] encfirstmsg = new byte[RSA_BLOCK_LENGTH];
        byte[] firstmsg = null;
        if (DEBUG) {
            System.out.println("About to read the first msg");
        }
        try {
            int bytesRead = bis.read(encfirstmsg);
            while (bytesRead != RSA_BLOCK_LENGTH) {
                bytesRead += bis.read(encfirstmsg, bytesRead, RSA_BLOCK_LENGTH - bytesRead);
            }
            firstmsg = c.doFinal(encfirstmsg);
            if (DEBUG) {
                System.out.println("Server received first message: " +
                        new String(firstmsg, "UTF8"));
            }
        }
        catch (Exception e) {
            System.out.println("Error/Timeout while receiving the first message from the client.");
            return null;
        }

        //get the first nonce, and add 1
        byte[] recvNonce = new byte[NONCE_LENGTH];
        System.arraycopy(firstmsg, 0, recvNonce, 0, NONCE_LENGTH);
        BigInteger firstNonceNum = new BigInteger(recvNonce);
        if (DEBUG) {
            System.out.println("First nonce recv: " + firstNonceNum);
        }
        byte[] firstNonceNumPlusOne = firstNonceNum.add(BigInteger.ONE).toByteArray();
        if (DEBUG) {
            System.out.println("First nonce recv + 1: " + firstNonceNumPlusOne);
        }
        byte[] firstNonceNumPlusOneCorrectLen = new byte[8];
        System.arraycopy(firstNonceNumPlusOne, 0, firstNonceNumPlusOneCorrectLen, 0, NONCE_LENGTH);

        //extract the shared key
        byte[] recvkey = new byte[BLOWFISH_KEY_LENGTH_BYTES];
        System.arraycopy(firstmsg, NONCE_LENGTH, recvkey, 0, BLOWFISH_KEY_LENGTH_BYTES);
        SecretKey key = (SecretKey) new SecretKeySpec(recvkey, 0, BLOWFISH_KEY_LENGTH_BYTES, "Blowfish");
        if (DEBUG) {
            try {
                System.out.println("Key received: " + new String(key.getEncoded(), "UTF8"));
            }
            catch (Exception e) {
                //cannot happen, encoding is real.
            }
        }
        /*Zero out arrays that contained the key in RAW*/
        Arrays.fill(recvkey, (byte) 0x00);
        Arrays.fill(firstmsg, (byte) 0x00);;

        //create a second nonce to authenticate the client
        SecureRandom sr = null;
        try {
            sr = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (Exception e) {
            //cannot happen, valid alg.
        }
        byte[] sendNonce = new byte[NONCE_LENGTH];
        sr.nextBytes(sendNonce);
        if (DEBUG) {
            System.out.println("Second nonce created: " + new BigInteger(sendNonce));
        }
        //create an ivp
        byte[] iv = new byte[8];
        sr.nextBytes(iv);
        IvParameterSpec ivp = new IvParameterSpec(iv);

        /*Construct the second message*/
        //prepend the iv for the decrypter to know.
        byte[] secondmsg = new byte[NONCE_LENGTH + NONCE_LENGTH];
        try {
            os.write(iv);
            c = Cipher.getInstance("Blowfish/CFB8/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, key, ivp);
            System.arraycopy(firstNonceNumPlusOneCorrectLen, 0, secondmsg, 0, NONCE_LENGTH);
            System.arraycopy(sendNonce, 0, secondmsg, NONCE_LENGTH, NONCE_LENGTH);
            CipherOutputStream cos = new CipherOutputStream(new NonClosingCipherOutputStream(os), c);
            cos.write(secondmsg);
            cos.flush();
            cos.close();
        }
        catch (Exception e) {
            //all padding, alg are correct. Only possible error is io
            System.out.println("Error sending the second message");
            return null;
        }
        if (DEBUG) {
            try {
                System.out.println("Server sent the second message: " + new String(secondmsg, "UTF8"));
            }
            catch (Exception e) {
                //cannot happen, valid enc
            }
        }
        //get the new ivp for the third message
        iv = new byte[8];
        try {
            int bytesRead = is.read(iv);
            while (bytesRead != 8) {
                bytesRead += is.read(iv, bytesRead, 8 - bytesRead);
            }
        }
        catch (IOException ioe) {
            System.out.println("Error/Timeout while getting the third message.");
            return null;
        }
        ivp = new IvParameterSpec(iv);

        /*Receive the third message, check that nonce = nonce*2*/
        byte[] thirdmsg = new byte[NONCE_LENGTH];
        try {
            c.init(Cipher.DECRYPT_MODE, key, ivp);
            CipherInputStream cis = new CipherInputStream(is, c);
            int bytesRead = cis.read(thirdmsg);
            while (bytesRead != NONCE_LENGTH) {
                bytesRead += cis.read(thirdmsg, bytesRead, NONCE_LENGTH - bytesRead);
            }
        }
        catch (Exception e) {
            System.out.println("Error/Timeout while getting the third message.");
            return null;

        }
        BigInteger secondNonceNum = new BigInteger(sendNonce);
        byte[] secondNonceTimesTwo = secondNonceNum.shiftLeft(1).toByteArray();
        byte[] secondNonceTimesTwoCorrectLen = new byte[NONCE_LENGTH];
        System.arraycopy(secondNonceTimesTwo, 0, secondNonceTimesTwoCorrectLen, 0, NONCE_LENGTH);
        if (Arrays.equals(thirdmsg, secondNonceTimesTwoCorrectLen)) {
            if (DEBUG) {
                System.out.println("Success");
            }
            return new KeyNonceBundle(key, secondNonceNum, firstNonceNum);
        }
        else {
            if (DEBUG) {
                System.out.println("Failure");
            }
            return null;
        }
    }

    protected synchronized ResourceReference resolvePutListResource(ResourceReference r_target) {
        try {
            Resource target = r_target.lock();
            if (false)
                r_target = "";
            r_target.unlock();
            r_target.unlock();
        } catch (InvalidResourceException ex) {
            return null;
        } finally {
            r_target.unlock();
        }
        return r_target;
    }
}
