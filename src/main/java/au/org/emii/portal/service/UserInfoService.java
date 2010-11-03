/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package au.org.emii.portal.service;

import au.org.emii.portal.service.ServiceStatusCodes;
import au.org.emii.portal.session.PortalUser;

/**
 *
 * @author geoff
 */
public interface UserInfoService extends ServiceStatusCodes {

        /**
         * Create a PortalUser instance for the passed in username.  User info
         * (name, email, address, etc) will be fetched from mest for this user
         * and returned in the PortalUser object if the user is registered.
         *
         * If the user does not exist or there was an error getting the user
         * info, we will return null
         *
         * Looks up the user with the default UserManagementWebService instance
         *
         * @param username username to get info for
         * @return PortalUser instance, on successful lookup or null if user doesn't exist
         * or there was a problem talking to the server
         */
        public PortalUser userInfo(String username);
        
}