package Anonimaizer;

import java.util.ArrayList;
import java.util.List;

public class ServerList {
    private List<String> servers = new ArrayList<>();

    public List<String> getServers() {
        return this.servers;
    }

    public ServerList(List<String> servers) {
        this.servers = servers;
    }

    public ServerList(String server) {
        this.servers.add(server);
    }
}
