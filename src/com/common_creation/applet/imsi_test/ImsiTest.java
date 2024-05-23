package com.common_creation.applet.imsi_test;

import javacard.framework.*;
import uicc.access.FileView;
import uicc.access.UICCSystem;
import uicc.toolkit.*;
import uicc.usim.access.USIMConstants;

public class ImsiTest extends Applet implements ToolkitInterface, ToolkitConstants {
    private static final byte DCS_8_BIT_DATA = (byte) 0x04;
    private static final byte DEV_ID_ME = (byte) 0x82;
    private static final byte USIM_INITIALIZATION_AND_FULL_FILE_CHANGE_NOTIFICATION = (byte) 0x00;
    private static final byte UICC_RESET = (byte) 0x04;
    private static final byte INS_GET_CURRENT_IMSI = 0x10;
    private static final byte INS_GET_BACKED_UP_IMSI = 0x12;
    private static final byte INS_BACKUP_CURRENT_IMSI = 0x14;
    private static final byte INS_RESTORE_BACKED_UP_IMSI = 0x16;
    private static final byte INS_WRITE_IMSI = 0x18;
    private static final byte[] usimAID = {
        (byte) 0xA0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x87, (byte) 0x10, (byte) 0x02, (byte) 0xFF,
        (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x89, (byte) 0x03, (byte) 0x02, (byte) 0x00, (byte) 0x00};
    private static final byte[] invalidImsi = {
        (byte) 0x08, (byte) 0x49, (byte) 0x14, (byte) 0x19, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    private final byte[] readBuffer;
    private final byte[] tmpBuffer;
    private final byte[] backupBuffer = new byte[9];
    private FileView fileView;
    private ToolkitRegistry toolkitRegistry;
    private byte menuId;
    private final byte[] menuText = new byte[]{'I', 'M', 'S', 'I', ' ', 't', 'e', 's', 't'};
    private final byte[] subMenu0 = {'S', 'h', 'o', 'w', ' ', 'c', 'u', 'r', 'r', 'e', 'n', 't', ' ', 'I', 'M', 'S', 'I'};
    private final byte[] subMenu1 = {'S', 'h', 'o', 'w', ' ', 'b', 'a', 'c', 'k', 'e', 'd', '-', 'u', 'p', ' ', 'I', 'M', 'S', 'I'};
    private final byte[] subMenu2 = {'-', '-', '-', '-', '-', ' ', 'D', 'A', 'N', 'G', 'E', 'R', ' ', 'Z', 'O', 'N', 'E', ' ', '-', '-', '-', '-', '-'};
    private final byte[] subMenu3 = {'B', 'a', 'c', 'k', 'u', 'p', ' ', 'c', 'u', 'r', 'r', 'e', 'n', 't', ' ', 'I', 'M', 'S', 'I'};
    private final byte[] subMenu4 = {'R', 'e', 's', 't', 'o', 'r', 'e', ' ', 'I', 'M', 'S', 'I', ' ', 'f', 'r', 'o', 'm', ' ', 'b', 'a', 'c', 'k', 'u', 'p', ' ', 'a', 'n', 'd', ' ', 'r', 'e', 'f', 'r', 'e', 's', 'h'};
    private final byte[] subMenu5 = {'S', 'e', 't', ' ', 'i', 'n', 'v', 'a', 'l', 'i', 'd', ' ', 'I', 'M', 'S', 'I', ' ', 'a', 'n', 'd', ' ', 'r', 'e', 'f', 'r', 'e', 's', 'h'};
    private final byte[] ok = {'O', 'K'};
    private final Object[] subMenuItems = {subMenu0, subMenu1, subMenu2, subMenu3, subMenu4, subMenu5};

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
        ImsiTest imsiTest = new ImsiTest();
        imsiTest.register();

        imsiTest.toolkitRegistry = ToolkitRegistrySystem.getEntry();
        imsiTest.menuId = imsiTest.toolkitRegistry.initMenuEntry(imsiTest.menuText, (short) 0, (short) imsiTest.menuText.length, (byte) 0, false, (byte) 0, (short) 0);
    }

    private ImsiTest() {
        readBuffer = JCSystem.makeTransientByteArray((byte) 9, JCSystem.CLEAR_ON_RESET);
        tmpBuffer = JCSystem.makeTransientByteArray((byte) 18, JCSystem.CLEAR_ON_RESET);
    }

    public Shareable getShareableInterfaceObject(AID clientAID, byte parameter) {
        if (clientAID == null) {
            return this;
        }
        return null;
    }

    @Override
    public void process(APDU apdu) throws ISOException {
        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();

        if (buffer[ISO7816.OFFSET_CLA] != (byte) 0x80) {
            return;
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_GET_CURRENT_IMSI:
                readImsi();
                apdu.setOutgoing();
                apdu.setOutgoingLength((short) 9);
                apdu.sendBytesLong(readBuffer, (short) 0, (short) 9);
                break;
            case INS_GET_BACKED_UP_IMSI:
                Util.arrayCopy(backupBuffer, (short) 0, readBuffer, (short) 0, (short) backupBuffer.length);
                apdu.setOutgoing();
                apdu.setOutgoingLength((short) 9);
                apdu.sendBytesLong(readBuffer, (short) 0, (short) 9);
                break;
            case INS_BACKUP_CURRENT_IMSI:
                readImsi();
                Util.arrayCopy(readBuffer, (short) 0, backupBuffer, (short) 0, (short) readBuffer.length);
                break;
            case INS_RESTORE_BACKED_UP_IMSI:
                writeImsi(backupBuffer);
                break;
            case INS_WRITE_IMSI:
                if (buffer[ISO7816.OFFSET_LC] != (byte) 0x09) {
                    ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
                }
                short read = apdu.setIncomingAndReceive();
                short offset = (short) 0;
                while (read > 0) {
                    offset = Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, readBuffer, offset, read);
                    read = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
                }
                writeImsi(readBuffer);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    @Override
    public void processToolkit(short event) throws ToolkitException {
        if (event == EVENT_MENU_SELECTION) {
            EnvelopeHandler eh = EnvelopeHandlerSystem.getTheHandler();
            byte selectedId = eh.getItemIdentifier();

            if (selectedId == menuId) {
                Util.arrayFillNonAtomic(readBuffer, (short) 0, (short) readBuffer.length, (byte) 0);
                Util.arrayFillNonAtomic(tmpBuffer, (short) 0, (short) tmpBuffer.length, (byte) 0);

                switch (renderSubMenu(subMenuItems)) {
                    case 0:
                        readImsi();
                        bytesToHex(readBuffer, (short) 0, (short) readBuffer.length, tmpBuffer, (short) 0);
                        printTmpBuffer();
                        break;
                    case 1:
                        Util.arrayCopy(backupBuffer, (short) 0, readBuffer, (short) 0, (short) backupBuffer.length);
                        bytesToHex(readBuffer, (short) 0, (short) readBuffer.length, tmpBuffer, (short) 0);
                        printTmpBuffer();
                        break;
                    case 3:
                        readImsi();
                        Util.arrayCopy(readBuffer, (short) 0, backupBuffer, (short) 0, (short) readBuffer.length);
                        print(ok, (short) 0, (short) ok.length);
                        break;
                    case 4:
                        writeImsi(backupBuffer);
                        clearLocation();
                        refresh();
                        print(ok, (short) 0, (short) ok.length);
                        break;
                    case 5:
                        writeImsi(invalidImsi);
                        clearLocation();
                        refresh();
                        print(ok, (short) 0, (short) ok.length);
                        break;
                }
            }
        }
    }

    private short renderSubMenu(Object[] menu) {
        ProactiveHandler ph = ProactiveHandlerSystem.getTheHandler();
        ProactiveResponseHandler rh = ProactiveResponseHandlerSystem.getTheHandler();

        ph.init(PRO_CMD_SELECT_ITEM, (byte) 0, DEV_ID_ME);
        ph.appendTLV((byte) (TAG_ALPHA_IDENTIFIER | TAG_SET_CR), menuText, (short) 0, (short) menuText.length);

        for (short index = 0; index < (short) menu.length; index++) {
            ph.appendTLV((byte) (TAG_ITEM | TAG_SET_CR), (byte) index, (byte[]) menu[index], (short) 0, (short) ((byte[]) menu[index]).length);
        }
        ph.send();

        if (rh.getGeneralResult() == RES_CMD_PERF) {
            return rh.getItemIdentifier();
        }

        return 0xFF;
    }

    private void print(byte[] text, short offset, short length) {
        ProactiveHandler ph = ProactiveHandlerSystem.getTheHandler();
        ph.initDisplayText((byte) 0x81, DCS_8_BIT_DATA, text, offset, length);
        ph.send();
    }

    private void printTmpBuffer() {
        print(tmpBuffer, (short) 0, (short) 18);
        swapBufferHex(tmpBuffer);
        print(tmpBuffer, (short) 3, (short) 15);
    }

    private FileView getTheFileView() {
        if (fileView == null) {
            fileView = UICCSystem.getTheFileView(new AID(usimAID, (short) 0, (byte) 16), JCSystem.CLEAR_ON_RESET);
        }
        return fileView;
    }

    private void readImsi() {
        FileView fv = getTheFileView();
        fv.select(USIMConstants.FID_EF_IMSI);
        fv.readBinary((short) 0, readBuffer, (short) 0, (short) 9);
    }

    private void writeImsi(byte[] buffer) {
        FileView fv = getTheFileView();
        fv.select(USIMConstants.FID_EF_IMSI);
        fv.updateBinary((short) 0, buffer, (short) 0, (short) 9);
    }

    private void clearLocation() {
        Util.arrayFill(tmpBuffer, (short) 0, (short) 14, (byte) 0xFF);

        FileView fv = getTheFileView();
        fv.select(USIMConstants.FID_EF_LOCI);
        fv.updateBinary((short) 0, tmpBuffer, (short) 0, (short) 11);
        fv.select(USIMConstants.FID_EF_PSLOCI);
        fv.updateBinary((short) 0, tmpBuffer, (short) 0, (short) 14);
    }

    private static final byte[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static void bytesToHex(byte[] inBuffer, short inOffset, short inLength, byte[] outBuffer, short outOffset) {
        for (short i = inOffset, j = outOffset; i < (short) (inOffset + inLength); i++) {
            outBuffer[j++] = hex[(short) ((inBuffer[i] >> 4) & 0x0F)];
            outBuffer[j++] = hex[(short) (inBuffer[i] & 0x0F)];
        }
    }

    public void swapBufferHex(byte[] buffer) {
        byte b;
        for (short i = 0; i < (short) buffer.length; i += 2) {
            b = tmpBuffer[i];
            tmpBuffer[i] = tmpBuffer[(short) (i + 1)];
            tmpBuffer[(short) (i + 1)] = b;
        }
    }

    private void refresh() {
        ProactiveHandler ph = ProactiveHandlerSystem.getTheHandler();
        ph.init(PRO_CMD_REFRESH, USIM_INITIALIZATION_AND_FULL_FILE_CHANGE_NOTIFICATION, DEV_ID_ME);
        ph.send();

        ph.init(PRO_CMD_REFRESH, UICC_RESET, DEV_ID_ME);
        ph.send();
    }
}