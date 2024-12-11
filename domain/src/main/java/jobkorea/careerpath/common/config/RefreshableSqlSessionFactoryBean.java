package jobkorea.careerpath.common.config;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.io.Resource;

import lombok.SneakyThrows;

public class RefreshableSqlSessionFactoryBean extends SqlSessionFactoryBean implements DisposableBean {

	private static final Logger LOG = LoggerFactory.getLogger(RefreshableSqlSessionFactoryBean.class);

	private SqlSessionFactory proxy;
	private int interval = 1000;

	private Timer timer;
	private TimerTask task;

	private Resource[] mapperLocations;

	private boolean running = false;

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();

	@Override
	public void setMapperLocations(Resource... mapperLocations) {
		super.setMapperLocations(mapperLocations);
		this.mapperLocations = mapperLocations;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public void refresh() throws Exception {
		w.lock();
		try {
			super.afterPropertiesSet();
		} finally {
			w.unlock();
		}

		LOG.info("sqlMapClient refreshed.");
	}

	private void setRefreshable() {
		proxy = (SqlSessionFactory)Proxy.newProxyInstance(SqlSessionFactory.class.getClassLoader(),
				new Class[] {SqlSessionFactory.class}, new InvocationHandler() {
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						return method.invoke(getParentObject(), args);
					}
				});

		task = new TimerTask() {
			private Map<Resource, Long> map = new HashMap<>();

			@SneakyThrows(Exception.class)
			public void run() {
				if (isModified()) {
					refresh();
				}
			}

			private boolean isModified() {
				boolean retVal = false;

				if (mapperLocations != null) {
					for (int i = 0; i < mapperLocations.length; i++) {
						Resource mappingLocation = mapperLocations[i];
						retVal |= findModifiedResource(mappingLocation);
					}
				}

				return retVal;
			}

			private boolean findModifiedResource(Resource resource) {
				boolean retVal = false;
				List<String> modifiedResources = new ArrayList<>();

				try {
					long modified = resource.lastModified();

					if (map.containsKey(resource)) {
						long lastModified = map.get(resource);

						if (lastModified != modified) {
							map.put(resource, modified);
							modifiedResources.add(resource.getDescription());
							retVal = true;
						}
					} else {
						map.put(resource, modified);
					}
				} catch (IOException e) {
					LOG.error("caught exception", e);
				}
				return retVal;
			}
		};

		timer = new Timer(true);
		resetInterval();

	}

	private Object getParentObject() throws Exception {
		r.lock();
		try {
			return super.getObject();
		} finally {
			r.unlock();
		}
	}

	@Override
	public SqlSessionFactory getObject() throws Exception {
		if (this.proxy == null) {
			setRefreshable();
		}
		return this.proxy;
	}

	@Override
	public Class<? extends SqlSessionFactory> getObjectType() {
		return (this.proxy != null ? this.proxy.getClass() : SqlSessionFactory.class);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public void setCheckInterval(int ms) {
		interval = ms;

		if (timer != null) {
			resetInterval();
		}
	}

	private void resetInterval() {
		if (running) {
			timer.cancel();
			running = false;
		}
		if (interval > 0) {
			timer.schedule(task, 0, interval);
			running = true;
		}
	}

	public void destroy() throws Exception {
		timer.cancel();
	}
}
