/*
 *     THIS FILE AND PROJECT IS SUPPLIED FOR EDUCATIONAL PURPOSES ONLY.
 *
 *     This program is free software; you can redistribute it
 *     and/or modify it under the terms of the GNU General
 *     Public License as published by the Free Software
 *     Foundation; either version 2 of the License, or (at your
 *     option) any later version.
 *
 *     This program is distributed in the hope that it will be
 *     useful, but WITHOUT ANY WARRANTY; without even the
 *     implied warranty of MERCHANTABILITY or FITNESS FOR A
 *     PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General
 *     Public License along with this program; if not, write to
 *     the Free Software Foundation, Inc., 59 Temple Place,
 */
package ballistickemu.Tools;
import java.util.Random;
import ballistickemu.Types.StickClientRegistry;
/**
 *
 * @author Simon
 */
public class UIDTool {

    public static String GenerateUID(StickClientRegistry LobbyRegistry)
    {
        boolean Complete;
        Complete = false;
        String GenUID = "";
        do
        {
        Random R = new Random();

            String[] NewUIDSplitted = new String[3];
            byte[] TempByte = new byte[1];
            NewUIDSplitted[0] = "1";
            for (int i = 1; i < 3; i++)
            {
                if (R.nextInt(3) > 1)
                {
                    TempByte[0] = (byte)(R.nextInt(25) + 97);
                    NewUIDSplitted[i] = Character.toString(((char)(TempByte[0])));
                }
                else
                    NewUIDSplitted[i] = Integer.toString(R.nextInt(8) + 1);
            }
            GenUID = NewUIDSplitted[0] + NewUIDSplitted[1] + NewUIDSplitted[2];

            Complete = !(LobbyRegistry.UIDExists(GenUID));

        } while(!Complete);
        return GenUID;
    }

}
